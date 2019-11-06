package com.reedelk.file.commons;

import com.reedelk.file.exception.FileReadException;
import com.reedelk.file.exception.NotValidFileException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.Supplier;

import static com.reedelk.file.commons.Messages.FileReadComponent.FILE_IS_DIRECTORY;
import static com.reedelk.file.commons.Messages.FileReadComponent.FILE_LOCK_ERROR;
import static com.reedelk.runtime.api.commons.StackTraceUtils.rootCauseMessageOf;

public class ReadFrom {

    public static Publisher<byte[]> path(Path path, int bufferSize, ReadOptions readOptions) {

        if (Files.isDirectory(path)) {
            String message = FILE_IS_DIRECTORY.format(path.toString());
            throw new NotValidFileException(message);
        }

        OpenOption[] openOptions = FileOpenOptions.from(FileOperation.READ, readOptions.getLockType());

        // We must immediately create the channel and acquire any lock
        // on the file if the user has requested to lock the file, so that
        // the exception can be correctly propagated to the caller and it
        // will not be lazily thrown when the stream is consumed.

        FileChannel channel = null;
        try {

            channel = FileChannel.open(path, openOptions);

            if (LockType.LOCK.equals(readOptions.getLockType())) {
                RetryCommand.builder()
                        .function(doLock(path, channel))
                        .maxRetries(readOptions.getMaxRetryAttempts())
                        .waitTime(readOptions.getMaxRetryWaitTime())
                        .retryOn(OverlappingFileLockException.class)
                        .build()
                        .execute();
            }

        } catch (Exception exception) {

            CloseableUtils.closeSilently(channel);

            String message = FILE_LOCK_ERROR.format(path.toString(), rootCauseMessageOf(exception));

            throw new FileReadException(message, exception);

        }

        FileChannel finalChannel = channel;

        return Flux.create(fluxSink -> {

            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

                while (finalChannel.read(byteBuffer) > 0) {

                    byteBuffer.flip();

                    byte[] chunk = new byte[byteBuffer.remaining()];

                    byteBuffer.get(chunk);

                    byteBuffer.clear();

                    fluxSink.next(chunk);
                }

                fluxSink.complete();

            } catch (IOException exception) {

                fluxSink.error(exception);

            } finally {

                CloseableUtils.closeSilently(finalChannel);

            }
        });

    }

    private static Supplier<FileLock> doLock(Path path, FileChannel channel) {
        return () -> {
            try {
                return channel.lock();
            } catch (IOException exception) {
                String message = FILE_LOCK_ERROR.format(path.toString(), rootCauseMessageOf(exception));
                throw new FileReadException(message, exception);
            }
        };
    }
}
