package com.reedelk.esb.flow;

import com.reedelk.runtime.api.file.ModuleId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FlowBuilderContextTest {

    @Mock
    private Bundle bundle;

    @Test
    void shouldCorrectlyBuildModuleId() {
        // Given
        Mockito.doReturn(997L).when(bundle).getBundleId();
        FlowBuilderContext context = new FlowBuilderContext(bundle, null, null, null);

        // When
        ModuleId actualModuleId = context.instantiateModuleId();

        // Then
        assertThat(actualModuleId.get()).isEqualTo(997L);
    }
}