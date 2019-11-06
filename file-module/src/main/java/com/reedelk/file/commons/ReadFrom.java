package com.reedelk.file.commons;

import com.reedelk.runtime.api.exception.ESBException;
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

public class ReadFrom {

    public static class ReadOptions {
        public LockType lockType;
        public int maxRetryAttempts;
        public long maxRetryWaitTime;
    }

    public static Publisher<byte[]> path(Path path, int bufferSize, ReadOptions readOptions) {
        if (!Files.isReadable(path)) {
            throw new ESBException("File does not exists " + path.toString());
        }
        if (Files.isDirectory(path)) {
            throw new ESBException("File is a directory " + path.toString());
        }

        return Flux.create(fluxSink -> {

            OpenOption[] openOptions = FileOpenOptions.from(FileOperation.READ, readOptions.lockType);

            try (FileChannel channel = FileChannel.open(path, openOptions)) {

                if (LockType.LOCK.equals(readOptions.lockType)) {
                    new RetryCommand<FileLock>(readOptions.maxRetryAttempts, readOptions.maxRetryWaitTime)
                            .run(lockSupplier(channel), OverlappingFileLockException.class);
                }

                ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

                while (channel.read(byteBuffer) > 0) {
                    byteBuffer.flip();

                    byte[] chunk = new byte[byteBuffer.remaining()];

                    byteBuffer.get(chunk);

                    byteBuffer.clear();

                    fluxSink.next(chunk);
                }

                fluxSink.complete();

            } catch (IOException exception) {
                fluxSink.error(exception);
            }
        });
    }

    private static Supplier<FileLock> lockSupplier(FileChannel channel) {
        return () -> {
            try {
                return channel.lock();
            } catch (IOException e) {
                throw new ESBException(e);
            }
        };
    }
}
