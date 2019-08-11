package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModuleDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;

import static com.reedelk.esb.module.state.ModuleState.RESOLVED;
import static com.reedelk.esb.module.state.ModuleState.UNRESOLVED;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UpdateRegisteredComponentTest {

    private final long moduleId = 232L;
    private final String testModuleName = "TestModule";
    private final String testVersion = "1.0.0-SNAPSHOT";
    private final String testLocation = "file://location/test";

    private final String component1 = "com.reedelk.esb.testing.AwesomeComponent1";
    private final String component2 = "com.reedelk.esb.testing.AwesomeComponent2";
    private final String component3 = "com.reedelk.esb.testing.AwesomeComponent3";
    private final String component4 = "com.reedelk.esb.testing.AwesomeComponent4";

    @Mock
    private ModuleDeserializer deserializer;

    @Test
    void shouldRemoveComponentFromUnresolvedAndAddItToResolvedAndKeepStateToUnresolved() {
        // Given
        Collection<String> unresolvedComponents = asList(component1, component3);
        Collection<String> resolvedComponents = asList(component2, component4);

        Module module = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .version(testVersion)
                .deserializer(deserializer)
                .moduleFilePath(testLocation)
                .build();
        module.unresolve(unresolvedComponents, resolvedComponents);

        // When (component 2 is resolved)
        UpdateRegisteredComponent step = new UpdateRegisteredComponent(component1);
        Module unresolvedModule = step.run(module);

        // Then
        assertThat(unresolvedModule.state()).isEqualTo(UNRESOLVED);
        assertThat(unresolvedModule.resolvedComponents()).containsExactlyInAnyOrder(component1, component2, component4);
        assertThat(unresolvedModule.unresolvedComponents()).containsExactlyInAnyOrder(component3);
    }

    @Test
    void shouldRemoveComponentFromUnresolvedAndAddItToResolvedAndTransitionToStateResolved() {
        // Given
        Collection<String> unresolvedComponents = Collections.singletonList(component3);
        Collection<String> resolvedComponents = asList(component2, component4);

        Module module = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .version(testVersion)
                .deserializer(deserializer)
                .moduleFilePath(testLocation)
                .build();
        module.unresolve(unresolvedComponents, resolvedComponents);

        // When (component 3 is resolved)
        UpdateRegisteredComponent step = new UpdateRegisteredComponent(component3);
        Module resolvedModule = step.run(module);

        // Then
        assertThat(resolvedModule.state()).isEqualTo(RESOLVED);
        assertThat(resolvedModule.resolvedComponents()).containsExactlyInAnyOrder(component2, component3, component4);
    }

    @Test
    void shouldThrowExceptionIfModuleStateIsNotInstalled() {
        // Given
        Module module = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .version(testVersion)
                .deserializer(deserializer)
                .moduleFilePath(testLocation)
                .build();
        UpdateRegisteredComponent step = new UpdateRegisteredComponent(component3);

        // Expect
        assertThrows(IllegalStateException.class, () -> step.run(module));
    }

}