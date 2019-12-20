package com.reedelk.esb.services.resource;

import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import com.reedelk.runtime.api.resource.ResourceFile;
import com.reedelk.runtime.api.resource.ResourceNotFound;
import com.reedelk.runtime.api.resource.ResourceService;
import com.reedelk.runtime.api.script.ScriptEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyZeroInteractions;

@ExtendWith(MockitoExtension.class)
class DefaultResourceServiceTest {

    private final long testModuleId = 234L;
    private final ModuleContext moduleContext = new ModuleContext(testModuleId);

    @Mock
    private Message message;
    @Mock
    private FlowContext flowContext;
    @Mock
    private ScriptEngineService scriptEngineService;

    private ResourceService fileProvider;

    @BeforeEach
    void setUp() {
        fileProvider = new DefaultResourceService(scriptEngineService);
    }

    @Test
    void shouldCorrectlyReturnFileBytes() {
        // Given
        String content = "my content";
        ResourceDynamic resourceDynamic = resourceDynamicFrom("#['myTemplate' + '.html']", content);

        doReturn(Optional.of("myTemplate.html"))
                .when(scriptEngineService)
                .evaluate(resourceDynamic, flowContext, message);

        // When
        ResourceFile<byte[]> resourceFile = fileProvider.find(resourceDynamic, flowContext, message);


        // Then
        Publisher<byte[]> fileDataStream = resourceFile.data();
        StepVerifier.create(fileDataStream)
                .expectNextMatches(bytes -> Arrays.equals(content.getBytes(), bytes))
                .verifyComplete();
        assertThat(resourceFile.path()).isEqualTo("myTemplate.html");
    }

    @Test
    void shouldThrowFileNotFoundExceptionResourceDynamicIsNull() {
        // Given
        ResourceDynamic resourceDynamic = null;

        // When
        ResourceNotFound thrown = assertThrows(ResourceNotFound.class,
                () -> fileProvider.find(resourceDynamic, flowContext, message));

        // Then
        assertThat(thrown).isNotNull();
        assertThat(thrown).hasMessage("Resource could not be found: dynamic resource object was null");
        verifyZeroInteractions(scriptEngineService);
    }

    @Test
    void shouldThrowFileNotFoundExceptionWhenResourceDynamicEvaluatesEmpty() {
        // Given
        ResourceDynamic resourceDynamic = resourceDynamicFrom(null, "anything");

        doReturn(Optional.empty())
                .when(scriptEngineService)
                .evaluate(resourceDynamic, flowContext, message);

        // When
        ResourceNotFound thrown = assertThrows(ResourceNotFound.class,
                () -> fileProvider.find(resourceDynamic, flowContext, message));

        // Then
        assertThat(thrown).isNotNull();
        assertThat(thrown).hasMessage("Resource could not be found: dynamic resource path was=[null]");
    }

    @Test
    void shouldRethrowExceptionThrownWhenResourceLoaded() {
        // Given
        ResourceDynamic resourceDynamic = ResourceDynamic.from("does not matter", moduleContext);
        ResourceDynamic resourceDynamicProxy = new TestResourceDynamicProxyThrowingResourceNotFoundException(resourceDynamic);

        doReturn(Optional.of("/assets/templates/hello-template.html"))
                .when(scriptEngineService)
                .evaluate(resourceDynamicProxy, flowContext, message);

        // When
        ResourceNotFound thrown = assertThrows(ResourceNotFound.class,
                () -> fileProvider.find(resourceDynamicProxy, flowContext, message));

        // Then
        assertThat(thrown).isNotNull();
        assertThat(thrown).hasMessage("Could not find resource xyz");
    }

    private ResourceDynamic resourceDynamicFrom(Object body, String wantedContent) {
        ResourceDynamic resourceDynamic = ResourceDynamic.from(body, moduleContext);
        return new TestResourceDynamicProxy(resourceDynamic, wantedContent);
    }

    class TestResourceDynamicProxy extends ResourceDynamic {

        private final String expectedResult;

        TestResourceDynamicProxy(ResourceDynamic original, String expectedResult) {
            super(original);
            this.expectedResult = expectedResult;
        }

        @Override
        public Publisher<byte[]> data(String evaluatedPath) {
            return Mono.just(expectedResult.getBytes());
        }
    }

    class TestResourceDynamicProxyThrowingResourceNotFoundException extends ResourceDynamic {

        TestResourceDynamicProxyThrowingResourceNotFoundException(ResourceDynamic original) {
            super(original);
        }

        @Override
        public Publisher<byte[]> data(String evaluatedPath) {
            throw new ResourceNotFound("Could not find resource xyz");
        }
    }
}