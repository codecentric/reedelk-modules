package com.reedelk.rest.component;

import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.RestMethod.GET;
import static com.reedelk.runtime.api.message.type.MimeType.APPLICATION_JSON;
import static org.junit.Assert.fail;

class RestClientGetTest extends RestClientAbstractTest {

    private RestClient component = componentWith(baseURL, path, GET);

    @Test
    void shouldGetExecuteCorrectlyWhenResponse200() {
        // Given
        String responseBody = "{\"Name\":\"John\"}";

        mockServer.stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                        .withStatus(200)
                        .withBody(responseBody)));

        Message payload = MessageBuilder.get().build();

        // When
        CountDownLatch latch = new CountDownLatch(1);

        component.apply(payload, flowContext, new OnResult() {
            @Override
            public void onResult(Message message, FlowContext flowContext) {
                // Then
                // onResult is called by a I/O non blocking thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        assertContent(message, responseBody, APPLICATION_JSON);
                        latch.countDown();
                    }
                }).start();
            }

            @Override
            public void onError(Throwable throwable, FlowContext flowContext) {
                fail("Error");
                latch.countDown();
            }
        });

        try {
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
    }
}
