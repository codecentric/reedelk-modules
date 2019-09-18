package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.commons.EndOfData;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.ByteArrayStreamContent;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

public class StreamResponseConsumer extends AbstractAsyncResponseConsumer<Void> {

    private OnResult callback;
    private FlowContext context;
    private BlockingQueue<byte[]> queue = new LinkedTransferQueue<>();
    private ByteBuffer byteBuffer = ByteBuffer.allocate(16 * 1024);

    public StreamResponseConsumer(OnResult callback, FlowContext context) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
        // Map the response to message and create a flux
        Flux<byte[]> bytesStream = Flux.create(sink -> {

            try {

                byte[] take = queue.take();

                while (take != EndOfData.MARKER) {

                    sink.next(take);

                    take = queue.take();

                }

                sink.complete();

            } catch (Exception e) {

                sink.error(e);

            } finally {
                queue = null;
            }

            // We must subscribe on a different scheduler because
            // otherwise we would block the HTTP server NIO Thread.
        }).subscribeOn(Schedulers.elastic()).cast(byte[].class);

        Message build = MessageBuilder.get().build();

        ByteArrayStreamContent content = new ByteArrayStreamContent(bytesStream, new Type(MimeType.APPLICATION_JSON));

        build.setContent(content);

        callback.onResult(build, context);
    }

    @Override
    protected void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        // TODO: check error handling
        try {

            decoder.read(byteBuffer);

            byteBuffer.flip();

            byte[] destination = new byte[byteBuffer.remaining()];

            byteBuffer.get(destination);

            byteBuffer.clear();

            queue.offer(destination);

            if (decoder.isCompleted()) {

                queue.offer(EndOfData.MARKER);

            }

        } catch (Exception e) {

            queue.offer(EndOfData.MARKER);

        }
    }


    @Override
    protected void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {

    }

    @Override
    protected Void buildResult(HttpContext context) throws Exception {
        //  Nothing to do
        return null;
    }

    @Override
    protected void releaseResources() {
        context = null;
        callback = null;
        byteBuffer = null;
    }
}
