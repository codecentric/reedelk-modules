package com.reedelk.file.write;

import com.reedelk.file.commons.CloseableUtils;
import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.*;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class Writer {

    public void writeTo(WriteConfiguration configuration,
                        FlowContext flowContext,
                        OnResult callback,
                        Path path,
                        TypedPublisher<byte[]> stream) {

        int bufferLength = configuration.getWriteBufferSize();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferLength);
        Flux.from(Flux.from(stream))
                .reduceWith(() -> {
                    try {
                        return FileChannel.open(path, configuration.getWriteMode().options());
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }, (channel, byteChunk) -> {

                    try {

                        // Write it in multiple steps
                        int remaining;
                        int offset = 0;
                        int length = byteChunk.length > bufferLength ? bufferLength : byteChunk.length;

                        while (length > 0) {

                            byteBuffer.clear();

                            byteBuffer.put(byteChunk, offset, length);

                            byteBuffer.flip();

                            channel.write(byteBuffer);

                            // If offset + length == bufferLength we are done.

                            offset += bufferLength;

                            remaining = byteChunk.length - offset;

                            length = remaining > bufferLength ? bufferLength : remaining;

                        }

                        return channel;

                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .doOnSuccessOrError((out, throwable) -> {

                    CloseableUtils.closeSilently(out);

                    if (throwable != null) {
                        // An exception was thrown during the process.
                        callback.onError(new ESBException(throwable), flowContext);

                    } else {

                        MessageAttributes attributes = new DefaultMessageAttributes(ImmutableMap.of(
                                FileWriteAttribute.FILE_NAME, path.toString(),
                                FileWriteAttribute.TIMESTAMP, System.currentTimeMillis()));

                        Message outMessage = MessageBuilder.get().attributes(attributes).empty().build();

                        callback.onResult(outMessage, flowContext);
                    }
                }).subscribe();
    }

}
