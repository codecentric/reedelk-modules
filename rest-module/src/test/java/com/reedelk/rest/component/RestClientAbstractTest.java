package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.reedelk.rest.client.DefaultHttpClientService;
import com.reedelk.rest.client.HttpClientService;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static org.junit.Assert.fail;

@ExtendWith(MockitoExtension.class)
abstract class RestClientAbstractTest {

    @Mock
    protected ScriptEngineService scriptEngine;
    @Mock
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

    protected RestClient componentWith(RestMethod method, String baseURL, String path) {
        RestClient restClient = new RestClient();
        restClient.setBaseURL(baseURL);
        restClient.setMethod(method);
        restClient.setPath(path);
        setScriptEngine(restClient);
        setHttpClientService(restClient);
        return restClient;
    }

    protected RestClient componentWith(RestMethod method, String baseURL, String path, String body) {
        RestClient restClient = componentWith(method, baseURL, path);
        restClient.setBody(body);
        return restClient;
    }

    private void setScriptEngine(RestClient restClient) {
        setField(restClient, "scriptEngine", scriptEngine);
    }

    private void setHttpClientService(RestClient restClient) {
        setField(restClient, "httpClientService", httpClientService);
    }

    private void setField(RestClient client, String fieldName, Object object) {
        try {
            Field field = client.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(client, object);
        } catch (NoSuchFieldException e) {
            fail(String.format("Field '%s' could not be found", fieldName));
        } catch (IllegalAccessException e) {
            fail(String.format("Could not access field '%s'", fieldName));
        }
    }
}
