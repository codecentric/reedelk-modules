package com.reedelk.rest.client.strategy;

import com.reedelk.rest.commons.DataMarker;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import static reactor.core.scheduler.Schedulers.elastic;

class StreamRequestProducer extends BasicAsyncRequestProducer {

    StreamRequestProducer(HttpHost target, HttpEntityEnclosingRequest request, Publisher<byte[]> stream) {
        super(target, request, new StreamProducer(stream));
    }

    static class StreamProducer implements HttpAsyncContentProducer {

        private Throwable throwable;

        private BlockingQueue<byte[]> queue = new LinkedTransferQueue<>();

        private ByteBuffer byteBuffer = ByteBuffer.allocate(16 * 1024);

        StreamProducer(Publisher<byte[]> stream) {

            Flux.from(stream).subscribeOn(elastic())

                    .doOnComplete(() -> queue.offer(DataMarker.END))

                    .doOnError((throwable) -> {

                        this.throwable = throwable;

                        queue.offer(DataMarker.ERROR);

                    })
                    .subscribe(bytes -> queue.offer(bytes));
        }

        @Override
        public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
            try {

                byte[] take = queue.take();

                if (take == DataMarker.ERROR) {

                    queue = null;

                    throw new IOException(throwable);

                } else if (take == DataMarker.END) {

                    encoder.complete();

                    queue = null;

                } else {

                    byteBuffer.put(take);

                    byteBuffer.flip();

                    encoder.write(byteBuffer);

                    byteBuffer.clear();

                }

            } catch (InterruptedException e) {

                encoder.complete();

                queue = null;

            }
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public void close() throws IOException {

        }
    }
}
