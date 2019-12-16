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
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

        Flux.from(dataStream)
                // This data stream is executed from originator Thread (which could be nio Thread or flow thread and so on).
                // Since we MUST execute this asynchronously (otherwise we might end up blocking a nio Thread - e.g from a rest call -
                // we must subscribe the stream from an elastic Thread. If we don't do it we might block
                // the NIO thread indefinitely. Note that the reduceWith is executed within the publishOn thread,
                // while the accumulator BiFunction is executed within the source Thread (e.g NIO thread).
                // The success callback is executed within the source Thread, while the onError from the elastic thread.

                .reduceWith(() -> {
                    try {
                        FileChannel fileChannel = FileChannelProvider.from(path,
                                config.getLockType(),
                                config.getRetryMaxAttempts(),
                                config.getRetryWaitTime(),
                                config.getWriteMode().options());

                        // We only allocate the buffer object if the FileChannel
                        // has been correctly opened.
                        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferLength);
                        return new Initial(byteBuffer, fileChannel);

                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }

                }, (initial, byteChunk) -> {

                    try {

                        // Write it in multiple steps
                        int remaining;
                        int offset = 0;
                        int length = byteChunk.length > bufferLength ? bufferLength : byteChunk.length;

                        while (length > 0) {

                            initial.buffer.clear();

                            initial.buffer.put(byteChunk, offset, length);

                            initial.buffer.flip();

                            initial.fileChannel.write(initial.buffer);

                            offset += bufferLength;

                            remaining = byteChunk.length - offset;

                            length = remaining > bufferLength ? bufferLength : remaining;

                        }

                        return initial;

                    } catch (Exception e) {
                        // Do on success or error is not called if an exception
                        // occurred here. Therefore we MUST clean up byte buffer
                        // AND close file channel here.
                        cleanUp(initial);
                        throw Exceptions.propagate(e);
                    }


                }).doOnSuccessOrError((initial, throwable) -> {

            // We must always and in any case (success or error) close the file channel.
            cleanUp(initial);

        }).doOnError(throwable -> {

            // On error map the exception and invoke the error callback.
            Exception realException = mapException(path, throwable);
            callback.onError(realException, flowContext);

        }).doOnSuccess(initial -> {

            // On success build the message and invoke the callback.
            MessageAttributes attributes = new DefaultMessageAttributes(FileWrite.class,
                    of(FILE_NAME, path.toString(), TIMESTAMP, System.currentTimeMillis()));
            Message outMessage = MessageBuilder.get().attributes(attributes).empty().build();

            callback.onResult(outMessage, flowContext);

        }).subscribeOn(Schedulers.elastic())
                .subscribe(); // Immediately fire the writing into the buffer
    }

    private Exception mapException(Path path, Throwable throwable) {
        if (throwable instanceof NoSuchFileException) {
            String message = ERROR_FILE_NOT_FOUND.format(path.toString());
            return new NotValidFileException(message, throwable);

        } else if (throwable instanceof MaxRetriesExceeded) {
            String message = FILE_LOCK_MAX_RETRY_ERROR.format(path.toString(), rootCauseMessageOf(throwable));
            return new FileWriteException(message, throwable);

        } else if (throwable instanceof FileAlreadyExistsException) {
            String message = ERROR_FILE_WRITE_ALREADY_EXISTS.format(path.toString());
            return new FileWriteException(message, throwable);

        } else {
            String errorMessage = ERROR_FILE_WRITE_WITH_PATH.format(path.toString(), rootCauseMessageOf(throwable));
            return new FileWriteException(errorMessage, throwable);
        }
    }

    private void cleanUp(Initial initial) {
        if (initial != null) {
            if (initial.buffer != null) initial.buffer.clear();
            initial.buffer = null;

            CloseableUtils.closeSilently(initial.fileChannel);
            initial.fileChannel = null;
        }
    }

    /**
     * An object keeping a pair of buffer and file channel used in the reduce step
     * to avoid creating a buffer before knowing if the file channel could be successfully
     * opened.
     */
    class Initial {
        ByteBuffer buffer;
        FileChannel fileChannel;

        Initial(ByteBuffer buffer, FileChannel fileChannel) {
            this.buffer = buffer;
            this.fileChannel = fileChannel;
        }
    }
}
