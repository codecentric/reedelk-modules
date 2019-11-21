package com.reedelk.esb.commons;

import com.reedelk.runtime.api.service.ConfigurationService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigPropertyAwareTypeFactoryTest {

    @Mock
    private ConfigurationService configurationService;

    private ConfigPropertyAwareTypeFactory typeFactory;

    @BeforeEach
    void setUp() {
        typeFactory = new ConfigPropertyAwareTypeFactory(configurationService);
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
        Object typeInstance = typeFactory.create(int.class, componentDefinition, configKey);

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
        Object typeInstance = typeFactory.create(int.class, componentDefinition, configKey);

        // Then
        assertThat(typeInstance).isEqualTo(expectedValue);
        verifyNoMoreInteractions(configurationService);
    }
}