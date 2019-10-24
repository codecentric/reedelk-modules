package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModuleDeserializer;
import com.reedelk.esb.module.state.ModuleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.reedelk.esb.module.state.ModuleState.INSTALLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class TransitionToInstalledTest {

    @Mock
    private ModuleDeserializer deserializer;

    private TransitionToInstalled step;

    @BeforeEach
    void setUp() {
        step = spy(new TransitionToInstalled());
    }

    @Test
    void shouldTransitionModuleToInstalledState() {
        // Given
        Module inputModule = Module.builder()
                .moduleId(33L)
                .name("StopTestModule")
                .version("1.0.0-SNAPSHOT")
                .deserializer(deserializer)
                .moduleFilePath("file://location/test")
                .build();
        inputModule.unresolve(Collections.emptyList(), Collections.emptyList());
        inputModule.resolve(Collections.emptyList());

        assumeTrue(inputModule.state() == ModuleState.RESOLVED, "Expected module to be in resolved state");

        // When
        Module actualModule = step.run(inputModule);

        // Then
        assertThat(actualModule.state()).isEqualTo(INSTALLED);
    }
}
