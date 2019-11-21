package com.reedelk.esb.flow;

import com.reedelk.esb.commons.ConfigPropertyAwareTypeFactory;
import com.reedelk.runtime.api.file.ModuleId;
import com.reedelk.runtime.api.service.ConfigurationService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class FlowBuilderContextTest {

    @Mock
    private Bundle bundle;
    @Mock
    private ConfigurationService configurationService;

    private ConfigPropertyAwareTypeFactory typeFactory;

    @BeforeEach
    void setUp() {
        typeFactory = spy(new ConfigPropertyAwareTypeFactory(configurationService));
    }

    @Test
    void shouldCorrectlyDelegateToTypeFactoryCreationOfModuleId() {
        // Given
        JSONObject componentDefinition = new JSONObject();
        long expectedModuleId = 997L;
        doReturn(expectedModuleId).when(bundle).getBundleId();
        doReturn((ModuleId) () -> expectedModuleId)
                .when(typeFactory)
                .create(ModuleId.class, componentDefinition, null, expectedModuleId);

        FlowBuilderContext context = new FlowBuilderContext(bundle, null, null, typeFactory);

        // When
        ModuleId actualModuleId = (ModuleId) context.create(ModuleId.class, componentDefinition);

        // Then
        assertThat(actualModuleId.get()).isEqualTo(expectedModuleId);
    }
}