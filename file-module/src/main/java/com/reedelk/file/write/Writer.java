package com.reedelk.file.write;

import com.reedelk.file.commons.AcquireLock;
import com.reedelk.file.commons.CloseableUtils;
import com.reedelk.file.commons.LockType;
import com.reedelk.file.commons.RetryCommand;
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
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;

public class Writer {

    public void writeTo(WriteConfiguration config,
                        FlowContext flowContext, OnResult callback, Path path, TypedPublisher<byte[]> dataStream) {

        int bufferLength = config.getWriteBufferSize();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferLength);

        Flux.from(Flux.from(dataStream))
                .reduceWith(() -> {

                    try {
                        FileChannel channel = FileChannel.open(path, config.getWriteMode().options());

                        if (LockType.LOCK.equals(config.getLockType())) {
                            RetryCommand.builder()
                                    .function(AcquireLock.from(path, channel))
                                    .maxRetries(config.getRetryMaxAttempts())
                                    .waitTime(config.getRetryWaitTime())
                                    .retryOn(OverlappingFileLockException.class)
                                    .build()
                                    .execute();
                        }

                        return channel;

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

                }).subscribe(); // Immediately fire the writing into the buffer
    }
}
