package com.reedelk.file.write;

import com.reedelk.file.commons.CloseableUtils;
import com.reedelk.file.commons.FileChannelProvider;
import com.reedelk.file.component.FileWrite;
import com.reedelk.file.exception.FileWriteException;
import com.reedelk.file.exception.MaxRetriesExceeded;
import com.reedelk.file.exception.NotValidFileException;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.*;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static com.reedelk.file.commons.Messages.FileWriteComponent.*;
import static com.reedelk.file.commons.Messages.Misc.FILE_LOCK_MAX_RETRY_ERROR;
import static com.reedelk.file.write.FileWriteAttribute.FILE_NAME;
import static com.reedelk.file.write.FileWriteAttribute.TIMESTAMP;
import static com.reedelk.runtime.api.commons.ImmutableMap.of;
import static com.reedelk.runtime.api.commons.StackTraceUtils.rootCauseMessageOf;

public class Writer {

    public void write(WriteConfiguration config,
                      FlowContext flowContext,
                      OnResult callback,
                      Path path,
                      TypedPublisher<byte[]> dataStream) {

        int bufferLength = config.getWriteBufferSize();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferLength);

        Flux.from(dataStream).reduceWith(() -> {
                    try {
                        return FileChannelProvider.from(path,
                                config.getLockType(),
                                config.getRetryMaxAttempts(),
                                config.getRetryWaitTime(),
                                config.getWriteMode().options());
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }

                }, (fileChannel, byteChunk) -> {

                    try {
                        // Write it in multiple steps
                        int remaining;
                        int offset = 0;
                        int length = byteChunk.length > bufferLength ? bufferLength : byteChunk.length;

                        while (length > 0) {

                            byteBuffer.clear();

                            byteBuffer.put(byteChunk, offset, length);

                            byteBuffer.flip();

                            fileChannel.write(byteBuffer);

                            offset += bufferLength;

                            remaining = byteChunk.length - offset;

                            length = remaining > bufferLength ? bufferLength : remaining;

                        }

                        return fileChannel;

                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                })

                .doOnSuccessOrError((fileChannel, throwable) -> {

                    CloseableUtils.closeSilently(fileChannel);

                    if (throwable != null) {

                        Exception realException;

                        if (throwable instanceof NoSuchFileException) {
                            String message = ERROR_FILE_NOT_FOUND.format(path.toString());
                            realException = new NotValidFileException(message, throwable);

                        } else if (throwable instanceof MaxRetriesExceeded) {
                            String message = FILE_LOCK_MAX_RETRY_ERROR.format(path.toString(), rootCauseMessageOf(throwable));
                            realException = new FileWriteException(message, throwable);

                        } else if (throwable instanceof FileAlreadyExistsException) {
                            String message = ERROR_FILE_WRITE_ALREADY_EXISTS.format(path.toString());
                            realException = new FileWriteException(message, throwable);

                        } else {
                            String errorMessage = ERROR_FILE_WRITE_WITH_PATH.format(path.toString(), rootCauseMessageOf(throwable));
                            realException = new FileWriteException(errorMessage, throwable);
                        }

                        callback.onError(realException, flowContext);

                    } else {

                        MessageAttributes attributes = new DefaultMessageAttributes(FileWrite.class,
                                of(FILE_NAME, path.toString(), TIMESTAMP, System.currentTimeMillis()));

                        Message outMessage = MessageBuilder.get().attributes(attributes).empty().build();

                        callback.onResult(outMessage, flowContext);
                    }

                }).subscribe(); // Immediately fire the writing into the buffer
    }
}
