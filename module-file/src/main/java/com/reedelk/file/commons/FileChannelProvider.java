package com.reedelk.file.commons;

import com.reedelk.file.exception.FileReadException;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.Supplier;

import static com.reedelk.file.commons.Messages.FileReadComponent.FILE_LOCK_ERROR;
import static com.reedelk.runtime.api.commons.StackTraceUtils.rootCauseMessageOf;

public class FileChannelProvider {

    public static FileChannel from(Path path, LockType lockType, int retryMaxAttempts, long retryWaitTime, OpenOption ...options) throws IOException {
        FileChannel channel = FileChannel.open(path, options);
        if (LockType.LOCK.equals(lockType)) {
            // If something goes wrong while acquiring the lock,
            // we must close the channel. This is because if we
            // can't acquire the lock, the channel is still open.
            try {
                RetryCommand.builder()
                        .function(from(path, channel))
                        .maxRetries(retryMaxAttempts)
                        .waitTime(retryWaitTime)
                        .retryOn(OverlappingFileLockException.class)
                        .build()
                        .execute();
            } catch (Exception exception) {
                CloseableUtils.closeSilently(channel);
                throw exception;
            }
        }
        return channel;
    }

    private static Supplier<FileLock> from(Path path, FileChannel channel) {
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
