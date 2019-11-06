package com.reedelk.file.commons;

import com.reedelk.runtime.api.exception.ESBException;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class ReadFrom {

    private static final Logger logger = LoggerFactory.getLogger(ReadFrom.class);

    public static Publisher<byte[]> path(Path path, int bufferSize, ReadOptions readOptions) {

        if (Files.isDirectory(path)) {
            throw new ESBException("Could not read file, it is a directory " + path.toString());
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
                        .function(doLock(channel))
                        .maxRetries(readOptions.getMaxRetryAttempts())
                        .waitTime(readOptions.getMaxRetryWaitTime())
                        .retryOn(OverlappingFileLockException.class)
                        .build()
                        .execute();
            }

        } catch (Exception exception) {

            CloseableUtils.closeSilently(channel);

            throw new ESBException(exception);

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

    private static Supplier<FileLock> doLock(FileChannel channel) {
        return () -> {
            try {
                return channel.lock();
            } catch (IOException e) {
                logger.warn("Could not lock file", e);
                throw new ESBException(e);
            }
        };
    }
}
