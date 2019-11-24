package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.reedelk.rest.client.DefaultHttpClientFactory;
import com.reedelk.rest.client.HttpClientFactory;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptBlockContext;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.reedelk.rest.utils.TestTag.INTEGRATION;
import static com.reedelk.runtime.api.commons.ScriptUtils.EVALUATE_PAYLOAD;
import static org.junit.Assert.fail;

@ExtendWith(MockitoExtension.class)
@Tag(INTEGRATION)
abstract class RestClientAbstractTest {

    @Mock
    protected ScriptEngineService scriptEngine;
    @Mock
    protected FlowContext flowContext;

    static final int PORT = 8181;
    static final String HOST = "localhost";
    static final String PATH = "/v1/resource";
    static final String BASE_URL = "http://" + HOST + ":" + PORT;

    final ScriptBlockContext scriptBlockContext = new ScriptBlockContext(10L, "aabbcc", "Test flow");;

    private static WireMockServer mockServer;

    DynamicByteArray EVALUATE_PAYLOAD_BODY = DynamicByteArray.from(EVALUATE_PAYLOAD, scriptBlockContext);

    private HttpClientFactory clientFactory = new DefaultHttpClientFactory();


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

    RestClient clientWith(RestMethod method, String baseURL, String path) {
        RestClient restClient = new RestClient();
        restClient.setBaseURL(baseURL);
        restClient.setMethod(method);
        restClient.setPath(path);
        setScriptEngine(restClient);
        setClientFactory(restClient);
        return restClient;
    }

    RestClient clientWith(RestMethod method, ClientConfiguration configuration, String path) {
        RestClient restClient = new RestClient();
        restClient.setConfiguration(configuration);
        restClient.setMethod(method);
        restClient.setPath(path);
        setScriptEngine(restClient);
        setClientFactory(restClient);
        return restClient;
    }

    RestClient clientWith(RestMethod method, String baseURL, String path, DynamicByteArray body) {
        RestClient restClient = clientWith(method, baseURL, path);
        restClient.setBody(body);
        return restClient;
    }

    RestClient clientWith(RestMethod method, ClientConfiguration configuration, String path, DynamicByteArray body) {
        RestClient restClient = clientWith(method, configuration, path);
        restClient.setBody(body);
        return restClient;
    }

    void invoke(RestClient component) {
        Message payload = MessageBuilder.get().build();
        component.apply(payload, flowContext, new OnResult() {});
    }

    private void setScriptEngine(RestClient restClient) {
        setField(restClient, "scriptEngine", scriptEngine);
    }

    private void setClientFactory(RestClient restClient) {
        setField(restClient, "clientFactory", clientFactory);
    }

    private void setField(RestClient client, String fieldName, Object object) {
        try {
            Field field = client.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(client, object);
        } catch (NoSuchFieldException e) {
            fail(String.format("Could not find  field '%s'", fieldName));
        } catch (IllegalAccessException e) {
            fail(String.format("Could not access field '%s'", fieldName));
        }
    }
}
