package com.reedelk.rest.component;

import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

class AssertThatHttpResponseContent {

    static void is(RestClient component,
                   Message message,
                   FlowContext context,
                   String expectedBody,
                   MimeType expectedMimeType) {

        CountDownLatch latch = new CountDownLatch(1);
        component.apply(message, context, new OnResult() {
            @Override
            public void onResult(Message message, FlowContext flowContext) {
                // Then (onResult is called by a I/O non blocking Thread)
                // We must consume the stream in order to compare the body,
                // therefore we cannot block the stream from a I/O blocking
                // thread and we consume it from a different thread.
                new Thread(() -> {
                    assertContent(message, expectedBody, expectedMimeType);
                    latch.countDown();
                }).start();
            }

            @Override
            public void onError(Throwable throwable, FlowContext flowContext) {
                fail(throwable.getMessage());
                latch.countDown();
            }
        });

        try {
            boolean await = latch.await(3, SECONDS);
            if(!await) fail("Timeout while waiting for response");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertContent(Message message, String expectedContent) {
        TypedContent<?> typedContent = message.getContent();
        String stringContent = typedContent.asString();
        assertThat(stringContent).isEqualTo(expectedContent);
    }

    private static void assertContent(Message message, String expectedContent, MimeType expectedMimeType) {
        assertContent(message, expectedContent);

        TypedContent<?> typedContent = message.getContent();
        Type type = typedContent.type();
        MimeType mimeType = type.getMimeType();
        assertThat(mimeType).isEqualTo(expectedMimeType);
    }
}
