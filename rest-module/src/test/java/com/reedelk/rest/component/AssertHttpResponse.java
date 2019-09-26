package com.reedelk.rest.component;

import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import org.assertj.core.api.Assertions;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class AssertHttpResponse {

    static void isSuccessful(RestClient client,
                             Message message,
                             FlowContext context,
                             String expectedBody,
                             MimeType expectedMimeType) {
        SuccessAssertion successAssertion = new SuccessAssertion(client, message, context, expectedBody, expectedMimeType);
        try {
            successAssertion.assertThat();
        } catch (InterruptedException e) {
            fail(e);
        }
    }

    static void isSuccessful(RestClient component,
                             Message message,
                             FlowContext context) {
        SuccessAssertion successAssertion = new SuccessAssertion(component, message, context);
        try {
            successAssertion.assertThat();
        } catch (InterruptedException e) {
            fail(e);
        }
    }

    static void isNotSuccessful(RestClient component,
                                Message message,
                                FlowContext context,
                                String expectedErrorMessage) {
        UnSuccessAssertion unSuccessAssertion = new UnSuccessAssertion(component, message, context, expectedErrorMessage);
        try {
            unSuccessAssertion.assertThat();
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

    private static class SuccessAssertion {

        private final Message message;
        private final String expectedBody;
        private final FlowContext context;
        private final RestClient component;
        private final MimeType expectedMimeType;

        private Message response;

        SuccessAssertion(RestClient component,
                         Message message,
                         FlowContext context,
                         String expectedBody,
                         MimeType expectedMimeType) {
            this.expectedMimeType = expectedMimeType;
            this.expectedBody = expectedBody;
            this.component = component;
            this.context = context;
            this.message = message;
        }

        SuccessAssertion(RestClient component,
                         Message message,
                         FlowContext context) {
            this(component, message, context, null, null);
        }

        void assertThat() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            component.apply(message, context, new OnResult() {
                @Override
                public void onResult(Message message, FlowContext flowContext) {
                    response = message;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable throwable, FlowContext flowContext) {
                    latch.countDown();
                }
            });

            boolean await = latch.await(3, SECONDS);

            if (!await) {
                fail("Timeout while waiting for response");
            }

            if (response != null) {
                if (expectedBody != null) {
                    assertContent(response, expectedBody, expectedMimeType);
                }
            } else {
                fail("Response was not successful");
            }
        }
    }

    private static class UnSuccessAssertion {

        private final Message message;
        private final FlowContext context;
        private final RestClient component;
        private final String expectedErrorMessage;

        private Throwable error;

        UnSuccessAssertion(RestClient component,
                         Message message,
                         FlowContext context,
                         String expectedErrorMessage) {
            this.expectedErrorMessage = expectedErrorMessage;
            this.component = component;
            this.message = message;
            this.context = context;
        }

        void assertThat() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            component.apply(message, context, new OnResult() {
                @Override
                public void onResult(Message message, FlowContext flowContext) {
                    latch.countDown();
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
                Assertions.assertThat(error).hasMessage(expectedErrorMessage);
            } else {
                fail("Response was successful");
            }
        }
    }
}
