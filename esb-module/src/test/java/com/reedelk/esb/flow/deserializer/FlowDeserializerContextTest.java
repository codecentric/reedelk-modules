package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.flow.deserializer.typefactory.ConfigPropertyAwareTypeFactoryDecorator;
import com.reedelk.esb.flow.deserializer.typefactory.ScriptBlockAwareTypeFactoryDecorator;
import com.reedelk.runtime.api.service.ConfigurationService;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.system.api.file.ModuleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FlowDeserializerContextTest {

    private long testModuleId = 997L;

    @Mock
    private Bundle bundle;
    @Mock
    private ConfigurationService configurationService;

    private TypeFactory typeFactory;

    @BeforeEach
    void setUp() {
        typeFactory = new ConfigPropertyAwareTypeFactoryDecorator(configurationService, TypeFactory.getInstance());
        typeFactory = new ScriptBlockAwareTypeFactoryDecorator(typeFactory, testModuleId, "aabbcc", "Test flow");
    }

    @Test
    void shouldCorrectlyDelegateToTypeFactoryCreationOfModuleId() {
        // Given
        FlowDeserializerContext context = new FlowDeserializerContext(bundle, null, null, typeFactory);

        // When
        ModuleId actualModuleId = context.typeFactory().create(ModuleId.class);

        // Then
        assertThat(actualModuleId.get()).isEqualTo(testModuleId);
    }
}