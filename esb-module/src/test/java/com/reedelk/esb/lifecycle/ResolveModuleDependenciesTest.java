package com.reedelk.esb.lifecycle;

import com.reedelk.esb.component.ComponentRegistry;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModuleDeserializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.reedelk.esb.module.state.ModuleState.*;
import static com.reedelk.esb.test.utils.Assertions.assertModuleErrorStateWith;
import static com.reedelk.esb.test.utils.TestJson.*;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResolveModuleDependenciesTest {

    @Mock
    private ModuleDeserializer deserializer;
    @Mock
    private ComponentRegistry componentRegistry;

    private ResolveModuleDependencies step;
    private Module aModule;

    @BeforeEach
    void setUp() {
        step = spy(new ResolveModuleDependencies());
        doReturn(componentRegistry).when(step).componentRegistry();
        aModule = Module.builder()
                .name("test")
                .moduleId(23L)
                .version("1.0.0-SNAPSHOT")
                .moduleFilePath("file:/module/path")
                .deserializer(deserializer)
                .build();
    }

    @Test
    void shouldReturnModuleWithStateInstalledWhenNoFlowsArePresent() {
        // Given
        DeserializedModule deserializedModule = new DeserializedModule(emptySet(), emptySet(), emptySet());
        doReturn(deserializedModule).when(deserializer).deserialize();

        // When
        Module module = step.run(aModule);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(INSTALLED);
    }

    @Test
    void shouldReturnModuleWithStateUnresolvedWhenNotAllFlowComponentsArePresent() {
        // Given
        doReturn(singletonList("com.reedelk.esb.not.found.Component"))
                .when(componentRegistry)
                .unregisteredComponentsOf(anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(FLOW_WITH_ROUTER.parse());

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule).when(deserializer).deserialize();

        // When
        Module module = step.run(aModule);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(UNRESOLVED);
    }

    @Test
    void shouldReturnModuleWithStateUnresolvedWhenNotAllSubFlowComponentsArePresent() {
        // Given
        doAnswer(invocation -> {
            final Collection<String> argument = (Collection<String>) (invocation.getArguments())[0];
            if (argument.containsAll(asList("com.reedelk.esb.test.utils.SubFlowComponent", "com.reedelk.esb.test.utils.AnotherSubFlowComponent"))) {
                return singletonList("com.reedelk.esb.test.utils.SubFlowComponent");
            }
            return emptyList();
        }).when(componentRegistry)
                .unregisteredComponentsOf(ArgumentMatchers.anyCollection());


        Set<JSONObject> flows = new HashSet<>();
        flows.add(FLOW_WITH_COMPONENTS.parse());

        Set<JSONObject> subFlows = new HashSet<>();
        subFlows.add(SUBFLOW_WITH_COMPONENTS.parse());

        DeserializedModule deserializedModule = new DeserializedModule(flows, subFlows, emptySet());
        doReturn(deserializedModule).when(deserializer).deserialize();

        // When
        Module module = step.run(aModule);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(UNRESOLVED);
    }

    @Test
    void shouldReturnModuleWithStateUnresolvedWhenNotAllConfigComponentsArePresent() {
        // Given
        doAnswer(invocation -> {
            final Collection<String> originalArgument = (Collection<String>) (invocation.getArguments())[0];
            if (originalArgument.contains("com.reedelk.esb.test.utils.TestConfiguration")) {
                return singletonList("com.reedelk.esb.test.utils.TestConfiguration");
            }
            return emptyList();
        }).when(componentRegistry)
                .unregisteredComponentsOf(ArgumentMatchers.anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(FLOW_WITH_COMPONENTS.parse());

        Set<JSONObject> config = new HashSet<>();
        config.add(CONFIG.parse());

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), config);
        doReturn(deserializedModule).when(deserializer).deserialize();

        // When
        Module module = step.run(aModule);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(UNRESOLVED);
    }

    @Test
    void shouldReturnModuleWithStateResolvedWhenAllComponentsArePresent() {
        // Given
        doReturn(emptyList())
                .when(componentRegistry)
                .unregisteredComponentsOf(anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(FLOW_WITH_ROUTER.parse());

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule).when(deserializer).deserialize();

        // When
        Module module = step.run(aModule);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(RESOLVED);
    }

    @Test
    void shouldReturnModuleWithStateErrorWhenDeserializationThrowsException() throws Exception {
        // Given
        doThrow(new JSONException("Could not deserialize module")).when(deserializer).deserialize();

        // When
        Module module = step.run(aModule);

        // Then
        assertModuleErrorStateWith(module, "Could not deserialize module");
    }

    @Test
    void shouldReturnModuleWithStateUnresolvedWithCorrectResolvedAndUnresolvedSets() throws Exception {
        // Given
        doReturn(asList("com.reedelk.esb.test.utils.AnotherTestComponent", "com.reedelk.esb.test.utils.TestInboundComponent"))
                .when(componentRegistry)
                .unregisteredComponentsOf(anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(FLOW_WITH_ROUTER.parse());

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule).when(deserializer).deserialize();

        // When
        Module module = step.run(aModule);

        // Then
        assertThat(module.state()).isEqualTo(UNRESOLVED);
        Collection<String> unresolvedComponents = module.unresolvedComponents();
        Collection<String> resolvedComponents = module.resolvedComponents();

        assertThat(unresolvedComponents).containsExactlyInAnyOrder(
                "com.reedelk.esb.test.utils.AnotherTestComponent",
                "com.reedelk.esb.test.utils.TestInboundComponent");

        assertThat(resolvedComponents).containsExactlyInAnyOrder(
                "com.reedelk.runtime.component.Router");
    }
}