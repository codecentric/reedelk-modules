package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.esb.module.DeSerializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.services.resource.ResourceLoader;
import com.reedelk.runtime.api.resource.ResourceBinary;
import com.reedelk.runtime.api.resource.ResourceText;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceResolverDecoratorTest {

    private final long testModuleId = 10L;
    private final TypeFactoryContext factoryContext = new TypeFactoryContext(testModuleId);

    @Mock
    private DeSerializedModule mockDeSerializedModule;
    @Mock
    private Module mockModule;

    private ResourceResolverDecorator decorator;

    @BeforeEach
    void setUp() {
        decorator = new ResourceResolverDecorator(TypeFactory.getInstance(), mockDeSerializedModule, mockModule);
    }

    @Test
    void shouldIsPrimitiveDelegate() {
        // When
        boolean actual = decorator.isPrimitive(String.class);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldCorrectlyLoadResourceText() {
        // Given
        String content = "my sample content";
        String propertyName = "myResourceText";
        String propertyValue = "/assets/template.html";

        JSONObject componentDefinition = new JSONObject();
        componentDefinition.put(propertyName, propertyValue);

        doReturn(singletonList(mockResourceLoader(propertyValue, content)))
                .when(mockDeSerializedModule)
                .getResources();

        // When
        ResourceText actualResource = decorator.create(ResourceText.class, componentDefinition, propertyName, factoryContext);

        // Then
        assertThat(actualResource.path()).isEqualTo(propertyValue);
        StepVerifier.create(actualResource.data())
                .expectNext(content)
                .verifyComplete();
    }

    @Test
    void shouldCorrectlyLoadResourceBinary() {
        // Given
        String content = "my sample content";
        String propertyName = "myResourceText";
        String propertyValue = "/assets/template.html";

        JSONObject componentDefinition = new JSONObject();
        componentDefinition.put(propertyName, propertyValue);

        doReturn(singletonList(mockResourceLoader(propertyValue, content)))
                .when(mockDeSerializedModule)
                .getResources();

        // When
        ResourceBinary actualResource = decorator.create(ResourceBinary.class, componentDefinition, propertyName, factoryContext);

        // Then
        assertThat(actualResource.path()).isEqualTo(propertyValue);
        StepVerifier.create(actualResource.data())
                .expectNextMatches(bytes -> Arrays.equals(content.getBytes(), bytes))
                .verifyComplete();
    }

    private ResourceLoader mockResourceLoader(String resourcePath, String resourceBody) {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        lenient()
                .doReturn("/Users/user/project/src/main/resources" + resourcePath)
                .when(resourceLoader)
                .getResourceFilePath();
        lenient()
                .doReturn(Mono.just(resourceBody.getBytes()))
                .when(resourceLoader)
                .body();
        return resourceLoader;
    }
}