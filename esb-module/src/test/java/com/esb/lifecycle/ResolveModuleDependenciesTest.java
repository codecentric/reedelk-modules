package com.esb.lifecycle;

import com.esb.commons.DeserializedModule;
import com.esb.component.ComponentRegistry;
import com.esb.flow.ModulesManager;
import com.esb.module.Module;
import com.esb.test.utils.TestFlow;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.esb.module.ModuleState.*;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ResolveModuleDependenciesTest extends AbstractLifecycleTest {

    @Mock
    private Bundle bundle;
    @Mock
    private ModulesManager modulesManager;
    @Mock
    private ComponentRegistry componentRegistry;

    private ResolveModuleDependencies step;

    @BeforeEach
    void setUp() {
        step = spy(new ResolveModuleDependencies(componentRegistry, modulesManager));
        doReturn(bundle).when(step).bundle();
        doReturn(new Version("1.0.0")).when(bundle).getVersion();
    }

    @Test
    void shouldReturnModuleWithStateInstalledWhenNoFlowsArePresent() {
        // Given
        doReturn(enumeration(emptyList()))
                .when(bundle)
                .getEntryPaths(anyString());

        // When
        Module module = step.run(null);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(INSTALLED);

        verify(modulesManager).add(module);
        verifyNoMoreInteractions(modulesManager);
    }

    @Test
    void shouldReturnModuleWithStateUnresolvedWhenNotAllComponentsArePresent() {
        // Given
        doReturn(singletonList("com.esb.not.found.Component"))
                .when(componentRegistry)
                .unregisteredComponentsOf(anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(parseFlow(TestFlow.WITH_CHOICE));

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule)
                .when(step)
                .deserializedModule(bundle);

        // When
        Module module = step.run(null);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(UNRESOLVED);

        verify(modulesManager).add(module);
        verifyNoMoreInteractions(modulesManager);
    }

    @Test
    void shouldReturnModuleWithStateResolvedWhenAllComponentsArePresent() {
        // Given
        doReturn(emptyList())
                .when(componentRegistry)
                .unregisteredComponentsOf(anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(parseFlow(TestFlow.WITH_CHOICE));

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule)
                .when(step)
                .deserializedModule(bundle);

        // When
        Module module = step.run(null);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(RESOLVED);

        verify(modulesManager).add(module);
        verifyNoMoreInteractions(modulesManager);
    }

    @Test
    void shouldReturnModuleWithStateErrorWhenDeserializationThrowsException() {
        // Given
        doThrow(new JSONException("Could not deserialize module"))
                .when(step)
                .deserializedModule(bundle);

        // When
        Module module = step.run(null);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(ERROR);

        verify(modulesManager).add(module);
        verifyNoMoreInteractions(modulesManager);
    }

    @Test
    void shouldReturnModuleWithStateUnresolvedWithCorrectResolvedAndUnresolvedSets() {
        // Given
        doReturn(asList("com.esb.test.utils.AnotherTestComponent", "com.esb.test.utils.TestInboundComponent"))
                .when(componentRegistry)
                .unregisteredComponentsOf(anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(parseFlow(TestFlow.WITH_CHOICE));

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule)
                .when(step)
                .deserializedModule(bundle);

        // When
        Module module = step.run(null);

        // Then
        assertThat(module.state()).isEqualTo(UNRESOLVED);
        Collection<String> unresolvedComponents = module.unresolvedComponents();
        Collection<String> resolvedComponents = module.resolvedComponents();

        assertThat(unresolvedComponents).containsExactlyInAnyOrder(
                "com.esb.test.utils.AnotherTestComponent",
                "com.esb.test.utils.TestInboundComponent");

        assertThat(resolvedComponents).containsExactlyInAnyOrder(
                "com.esb.component.Choice");
    }

}