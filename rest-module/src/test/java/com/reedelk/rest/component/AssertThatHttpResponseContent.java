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
import static org.junit.jupiter.api.Assertions.fail;

class AssertThatHttpResponseContent {

    static void is(RestClient component,
                   Message message,
                   FlowContext context,
                   String expectedBody,
                   MimeType expectedMimeType) {

        Asserter asserter = new Asserter(component, message, context, expectedBody, expectedMimeType);
        try {
            asserter.assertThat();
        } catch (InterruptedException e) {
            fail(e);
        }
    }

    private static void assertContent(Message message, String expectedContent, MimeType expectedMimeType) {
        assertContent(message, expectedContent);

        TypedContent<?> typedContent = message.getContent();
        Type type = typedContent.type();
        MimeType mimeType = type.getMimeType();
        assertThat(mimeType).isEqualTo(expectedMimeType);
    }

    private static void assertContent(Message message, String expectedContent) {
        TypedContent<?> typedContent = message.getContent();
        String stringContent = typedContent.asString();
        assertThat(stringContent).isEqualTo(expectedContent);
    }

    static class Asserter {

        private final MimeType expectedMimeType;
        private final String expectedBody;
        private final RestClient component;
        private final FlowContext context;
        private final Message message;

        private Throwable error;

        Asserter(RestClient component,
                 Message message,
                 FlowContext context,
                 String expectedBody,
                 MimeType expectedMimeType) {
            this.component = component;
            this.message = message;
            this.context = context;
            this.expectedBody = expectedBody;
            this.expectedMimeType = expectedMimeType;
        }

        void assertThat() throws InterruptedException {
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
                    error = throwable;
                    latch.countDown();
                }
            });


            boolean await = latch.await(3, SECONDS);
            if (!await) {
                fail("Timeout while waiting for response");
            }
            if (error != null) {
                fail(error);
            }
        }
    }
}
