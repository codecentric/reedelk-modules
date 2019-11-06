package com.reedelk.file.read;

import com.reedelk.file.commons.*;
import com.reedelk.file.exception.FileReadException;
import com.reedelk.file.exception.MaxRetriesExceeded;
import com.reedelk.file.exception.NotValidFileException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.Supplier;

import static com.reedelk.file.commons.Messages.FileReadComponent.*;
import static com.reedelk.runtime.api.commons.StackTraceUtils.rootCauseMessageOf;

public class Reader {

    public Publisher<byte[]> path(Path path, ReadConfiguration config) {

        if (Files.isDirectory(path)) {
            String message = FILE_IS_DIRECTORY.format(path.toString());
            throw new NotValidFileException(message);
        }

        OpenOption[] openOptions = FileOpenOptions.from(FileOperation.READ, config.getLockType());

        // We must immediately create the channel and acquire any lock
        // on the file if the user has requested to lock the file, so that
        // the exception can be correctly propagated to the caller and it
        // will not be lazily thrown when the stream is consumed.

        FileChannel channel = null;
        try {

            channel = FileChannel.open(path, openOptions);

            if (LockType.LOCK.equals(config.getLockType())) {
                RetryCommand.builder()
                        .function(doLock(path, channel))
                        .maxRetries(config.getRetryMaxAttempts())
                        .waitTime(config.getRetryWaitTime())
                        .retryOn(OverlappingFileLockException.class)
                        .build()
                        .execute();
            }

        } catch (NoSuchFileException exception) {

            CloseableUtils.closeSilently(channel);

            String message = FILE_NOT_FOUND.format(path.toString());

            throw new NotValidFileException(message, exception);

        } catch (Exception exception) {

            CloseableUtils.closeSilently(channel);

            if (exception instanceof FileReadException) {
                throw (FileReadException) exception;
            }

            if (exception instanceof MaxRetriesExceeded) {
                String message = FILE_READ_LOCK_MAX_RETRY_ERROR.format(path.toString(), rootCauseMessageOf(exception));
                throw new FileReadException(message, exception);
            }

            String message = FILE_READ_ERROR.format(path.toString(), rootCauseMessageOf(exception));
            throw new FileReadException(message, exception);
        }

        final int readBufferSize = config.getReadBufferSize();
        final FileChannel finalChannel = channel;

        return Flux.create(fluxSink -> {

            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(readBufferSize);

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

    private Supplier<FileLock> doLock(Path path, FileChannel channel) {
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
