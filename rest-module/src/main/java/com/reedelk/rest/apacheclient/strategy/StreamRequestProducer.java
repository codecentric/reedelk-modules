package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.commons.EndOfData;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import static reactor.core.scheduler.Schedulers.elastic;

class StreamRequestProducer extends BasicAsyncRequestProducer {

    StreamRequestProducer(HttpHost target, HttpEntityEnclosingRequest request, Flux<byte[]> stream) {
        super(target, request, new StreamProducer(stream));
    }

    static class StreamProducer implements HttpAsyncContentProducer {

        private BlockingQueue<byte[]> queue = new LinkedTransferQueue<>();

        private ByteBuffer byteBuffer = ByteBuffer.allocate(16 * 1024);

        StreamProducer(Flux<byte[]> stream) {
            stream.subscribeOn(elastic())
                    .doOnComplete(() -> queue.offer(EndOfData.MARKER))
                    .subscribe(bytes -> queue.offer(bytes));
        }

        @Override
        public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
            try {

                byte[] take = queue.take();

                if (take != EndOfData.MARKER) {

                    byteBuffer.put(take);

                    byteBuffer.flip();

                    encoder.write(byteBuffer);

                    byteBuffer.clear();

                } else {

                    encoder.complete();

                    queue = null;
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
