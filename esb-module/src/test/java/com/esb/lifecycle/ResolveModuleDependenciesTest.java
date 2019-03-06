package com.esb.lifecycle;

import com.esb.component.ComponentRegistry;
import com.esb.module.DeserializedModule;
import com.esb.module.Module;
import com.esb.module.ModuleDeserializer;
import com.esb.test.utils.TestFlow;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.esb.module.state.ModuleState.*;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResolveModuleDependenciesTest extends AbstractLifecycleTest {

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
    void shouldReturnModuleWithStateInstalledWhenNoFlowsArePresent() throws Exception {
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
    void shouldReturnModuleWithStateUnresolvedWhenNotAllComponentsArePresent() throws Exception {
        // Given
        doReturn(singletonList("com.esb.not.found.Component"))
                .when(componentRegistry)
                .unregisteredComponentsOf(anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(parseFlow(TestFlow.WITH_CHOICE));

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule).when(deserializer).deserialize();

        // When
        Module module = step.run(aModule);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(UNRESOLVED);
    }

    @Test
    void shouldReturnModuleWithStateResolvedWhenAllComponentsArePresent() throws Exception {
        // Given
        doReturn(emptyList())
                .when(componentRegistry)
                .unregisteredComponentsOf(anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(parseFlow(TestFlow.WITH_CHOICE));

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
        assertThat(module).isNotNull();
        assertThat(module.state()).isEqualTo(ERROR);
    }

    @Test
    void shouldReturnModuleWithStateUnresolvedWithCorrectResolvedAndUnresolvedSets() throws Exception {
        // Given
        doReturn(asList("com.esb.test.utils.AnotherTestComponent", "com.esb.test.utils.TestInboundComponent"))
                .when(componentRegistry)
                .unregisteredComponentsOf(anyCollection());

        Set<JSONObject> flows = new HashSet<>();
        flows.add(parseFlow(TestFlow.WITH_CHOICE));

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule).when(deserializer).deserialize();

        // When
        Module module = step.run(aModule);

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