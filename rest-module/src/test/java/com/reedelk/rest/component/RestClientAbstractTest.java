package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.reedelk.rest.client.DefaultHttpClientService;
import com.reedelk.rest.client.HttpClientService;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

abstract class RestClientAbstractTest {

    protected FlowContext flowContext;

    private static final int PORT = 8181;
    private static final String HOST = "localhost";

    private HttpClientService httpClientService = new DefaultHttpClientService();

    static WireMockServer mockServer;

    static String path = "/v1/resource";
    static String baseURL = "http://" + HOST + ":" + PORT;


    @BeforeAll
    static void setUpBeforeAll(){
        mockServer = new WireMockServer(PORT);
        mockServer.start();
        configureFor(HOST, PORT);
    }

    @AfterAll
    static void tearDownAfterAll() {
        mockServer.stop();
    }

    @BeforeEach
    void setUp() {
        mockServer.resetAll();
    }

    void assertContent(Message message, String expectedContent) {
        TypedContent<?> typedContent = message.getContent();
        String stringContent = typedContent.asString();
        assertThat(stringContent).isEqualTo(expectedContent);
    }

    void assertContent(Message message, String expectedContent, MimeType expectedMimeType) {
        assertContent(message, expectedContent);

        TypedContent<?> typedContent = message.getContent();
        Type type = typedContent.type();
        MimeType mimeType = type.getMimeType();
        assertThat(mimeType).isEqualTo(expectedMimeType);
    }

    RestClient componentWith(String baseURL, String path, RestMethod method) {
        RestClient restClient = new RestClient();
        restClient.setBaseURL(baseURL);
        restClient.setMethod(method);
        restClient.setPath(path);
        setHttpClientService(restClient, httpClientService);
        return restClient;
    }

    void setScriptEngine(RestClient restClient, ScriptEngineService service) {
        try {
            Field field = restClient.getClass().getDeclaredField("scriptEngine");
            field.setAccessible(true);
            field.set(restClient, service);
        } catch (NoSuchFieldException e) {
            fail("Field 'scriptEngine' could not be found");
        } catch (IllegalAccessException e) {
            fail("Could not access field 'scriptEngine'");
        }
    }

    void setHttpClientService(RestClient restClient, HttpClientService httpClientService) {
        try {
            Field field = restClient.getClass().getDeclaredField("httpClientService");
            field.setAccessible(true);
            field.set(restClient, httpClientService);
        } catch (NoSuchFieldException e) {
            fail("Field 'httpClientService' could not be found");
        } catch (IllegalAccessException e) {
            fail("Could not access field 'httpClientService'");
        }
    }
}
