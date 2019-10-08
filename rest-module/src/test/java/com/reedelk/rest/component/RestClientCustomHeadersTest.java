package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicmap.DynamicStringMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

class RestClientCustomHeadersTest extends RestClientAbstractTest {

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldCorrectlyAddCustomHeaders(String method) {
        // Given
        RestClient component = clientWith(RestMethod.valueOf(method), baseURL, path, EVALUATE_PAYLOAD_BODY);

        Map<String,String> additionalHeaders = new HashMap<>();
        additionalHeaders.put("X-Token", "123456789");
        additionalHeaders.put("Source", "test source");
        DynamicStringMap additionalHeadersMap = DynamicStringMap.from(additionalHeaders);
        component.setHeaders(additionalHeadersMap);

        doReturn(additionalHeaders)
                .when(scriptEngine)
                .evaluate(eq(additionalHeadersMap), any(Message.class), any(FlowContext.class));

        givenThat(WireMock.any(urlEqualTo(path))
                .withHeader("X-Token", equalTo("123456789"))
                .withHeader("Source", equalTo("test source"))
                .willReturn(aResponse().withStatus(200)));


        Message payload = MessageBuilder.get().empty().build();

        // Expect
        AssertHttpResponse
                .isSuccessful(component, payload, flowContext);
    }
}
