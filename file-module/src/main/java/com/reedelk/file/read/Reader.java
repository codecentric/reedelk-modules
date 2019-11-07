package com.reedelk.file.read;

import com.reedelk.file.commons.FileChannelProvider;
import com.reedelk.file.commons.FileOpenOptions;
import com.reedelk.file.commons.FileOperation;
import com.reedelk.file.exception.FileReadException;
import com.reedelk.file.exception.MaxRetriesExceeded;
import com.reedelk.file.exception.NotValidFileException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import static com.reedelk.file.commons.Messages.FileReadComponent.FILE_IS_DIRECTORY;
import static com.reedelk.file.commons.Messages.FileReadComponent.FILE_READ_ERROR;
import static com.reedelk.file.commons.Messages.Misc.FILE_LOCK_MAX_RETRY_ERROR;
import static com.reedelk.file.commons.Messages.Misc.FILE_NOT_FOUND;
import static com.reedelk.runtime.api.commons.StackTraceUtils.rootCauseMessageOf;

public class Reader {

    public Publisher<byte[]> read(Path path, ReadConfiguration config) {

        return Flux.create(sink -> {

            // This consumer is only created if the payload is used.

            if (Files.isDirectory(path)) {
                String message = FILE_IS_DIRECTORY.format(path.toString());
                throw new NotValidFileException(message);
            }

            OpenOption[] openOptions = FileOpenOptions.from(FileOperation.READ, config.getLockType());

            try (FileChannel channel = FileChannelProvider.from(
                    path,
                    config.getLockType(),
                    config.getRetryMaxAttempts(),
                    config.getRetryWaitTime(),
                    openOptions)) {

                int readBufferSize = config.getReadBufferSize();

                ByteBuffer byteBuffer = ByteBuffer.allocate(readBufferSize);

                while (channel.read(byteBuffer) > 0) {

                    byteBuffer.flip();

                    byte[] chunk = new byte[byteBuffer.remaining()];

                    byteBuffer.get(chunk);

                    byteBuffer.clear();

                    sink.next(chunk);
                }

                sink.complete();

            } catch (NoSuchFileException exception) {

                String message = FILE_NOT_FOUND.format(path.toString());

                throw new NotValidFileException(message);

            } catch (Exception exception) {

                if (exception instanceof FileReadException) {
                    throw (FileReadException) exception;
                }

                if (exception instanceof MaxRetriesExceeded) {
                    String message = FILE_LOCK_MAX_RETRY_ERROR.format(path.toString(), rootCauseMessageOf(exception));
                    throw new FileReadException(message, exception);
                }

                String message = FILE_READ_ERROR.format(path.toString(), rootCauseMessageOf(exception));
                throw new FileReadException(message, exception);

            }
        });
    }
}
