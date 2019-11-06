package com.reedelk.esb.services.file;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class StreamFrom {

    public static Publisher<byte[]> url(URL target, int bufferSize) {

        return Flux.create(fluxSink -> {

            try (ReadableByteChannel channel = Channels.newChannel(target.openStream())) {

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
}
