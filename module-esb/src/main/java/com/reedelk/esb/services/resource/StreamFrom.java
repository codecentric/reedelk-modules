package com.reedelk.esb.services.resource;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class StreamFrom {

    private StreamFrom() {
    }

    public static Publisher<byte[]> url(URL target, int bufferSize) {

        return Flux.create(sink -> {

            try (ReadableByteChannel channel = Channels.newChannel(target.openStream())) {

                ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

                while (channel.read(byteBuffer) > 0) {

                    byteBuffer.flip();

                    byte[] chunk = new byte[byteBuffer.remaining()];

                    byteBuffer.get(chunk);

                    byteBuffer.clear();

                    sink.next(chunk);
                }

                sink.complete();

            } catch (Exception exception) {
                // We MUST catch any exception. If we don't do it we risk to never close the sink,
                // hence never returning to the Source component which might wait indefinitely!
                sink.error(exception);

            }
        });
    }
}
