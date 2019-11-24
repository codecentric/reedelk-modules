package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.api.service.ConfigurationService;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import com.reedelk.runtime.system.api.file.ModuleId;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigPropertyAwareTypeFactoryDecoratorTest {

    @Mock
    private ConfigurationService configurationService;

    private ConfigPropertyAwareTypeFactoryDecorator typeFactory;
    private TypeFactoryContext typeFactoryContext = new TypeFactoryContext(10L, "aabbcc", "Test flow");

    @BeforeEach
    void setUp() {
        typeFactory = new ConfigPropertyAwareTypeFactoryDecorator(configurationService, TypeFactory.getInstance());
    }

    @Test
    void shouldDelegateConfigurationServiceWhenPropertyIsConfigProperty() {
        // Given
        String configKey = "myProperty";
        int expectedValue = 54;

        doReturn(expectedValue)
                .when(configurationService)
                .get("listener.port", int.class);
        JSONObject componentDefinition = new JSONObject();
        componentDefinition.put(configKey, "${listener.port}");

        // When
        Object typeInstance = typeFactory.create(int.class, componentDefinition, configKey, typeFactoryContext);

        // Then
        assertThat(typeInstance).isEqualTo(expectedValue);
        verify(configurationService).get("listener.port", int.class);
        verifyNoMoreInteractions(configurationService);
    }

    @Test
    void shouldReturnPropertyValueWhenItIsNotConfigProperty() {
        // Given
        String configKey = "myProperty";
        int expectedValue = 54;

        JSONObject componentDefinition = new JSONObject();
        componentDefinition.put(configKey, expectedValue);

        // When
        Object typeInstance = typeFactory.create(int.class, componentDefinition, configKey, typeFactoryContext);

        // Then
        assertThat(typeInstance).isEqualTo(expectedValue);
        verifyNoMoreInteractions(configurationService);
    }

    @Test
    void shouldReturnModuleIdTypeInstance() {
        // When
        ModuleId typeInstance = typeFactory.create(ModuleId.class, null, null, typeFactoryContext);

        // Then
        assertThat(typeInstance.get()).isEqualTo(typeFactoryContext.getModuleId());
        verifyNoMoreInteractions(configurationService);
    }
}