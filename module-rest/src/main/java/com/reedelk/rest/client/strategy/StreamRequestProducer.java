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

    StreamRequestProducer(HttpHost target, HttpEntityEnclosingRequest request, Publisher<byte[]> stream, int requestBufferSize) {
        super(target, request, new StreamProducer(stream, requestBufferSize));
    }

    static class StreamProducer implements HttpAsyncContentProducer {

        private int requestBufferSize;
        private Throwable throwable;
        private ByteBuffer byteBuffer;
        private BlockingQueue<byte[]> queue = new LinkedTransferQueue<>();

        StreamProducer(Publisher<byte[]> stream, int requestBufferSize) {

            this.requestBufferSize = requestBufferSize;

            this.byteBuffer = ByteBuffer.allocate(requestBufferSize);

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

                    byteBuffer = null;

                } else {

                    int remaining;

                    int offset = 0;

                    int length = take.length > requestBufferSize ? requestBufferSize : take.length;

                    while (length > 0) {

                        byteBuffer.clear();

                        byteBuffer.put(take, offset, length);

                        byteBuffer.flip();

                        encoder.write(byteBuffer);

                        offset += requestBufferSize;

                        remaining = take.length - offset;

                        length = remaining > requestBufferSize ? requestBufferSize : remaining;

                    }
                }

            } catch (InterruptedException e) {

                queue = null;

                Thread.currentThread().interrupt();

            }
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public void close() {

        }
    }
}
