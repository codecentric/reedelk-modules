package com.esb.lifecycle;

import com.esb.api.component.Component;
import com.esb.api.exception.ESBException;
import com.esb.commons.DeserializedModule;
import com.esb.component.Choice;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;
import com.esb.flow.ExecutionNode.ReferencePair;
import com.esb.flow.Flow;
import com.esb.flow.ModulesManager;
import com.esb.module.Module;
import com.esb.test.utils.AnotherTestComponent;
import com.esb.test.utils.TestComponent;
import com.esb.test.utils.TestFlow;
import com.esb.test.utils.TestInboundComponent;
import com.google.common.collect.ImmutableList;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.esb.module.state.ModuleState.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BuildModuleTest extends AbstractLifecycleTest {

    private final long moduleId = 232L;
    private final String testModuleName = "TestModule";
    private final String testVersion = "1.0.0-SNAPSHOT";
    private final String testLocation = "file://location/test";

    private final Collection<String> unresolvedComponents = asList("com.esb.UnresolvedComponent1", "com.esb.UnresolvedComponent1");
    private final Collection<String> resolvedComponents = asList("com.esb.ResolvedComponent1", "com.esb.ResolvedComponent2");

    @Mock
    private Flow flow;
    @Mock
    private Bundle bundle;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private ServiceObjects serviceObjects;
    @Mock
    private ModulesManager modulesManager;
    @Mock
    private ServiceReference<Component> serviceReference;

    private BuildModule step;

    @BeforeEach
    void setUp() {
        step = spy(new BuildModule(modulesManager));
        step.bundle(bundle);
        doReturn(bundleContext).when(bundle).getBundleContext();
        doReturn(bundle).when(bundleContext).getBundle(moduleId);
    }

    @Test
    void shouldNotBuildModuleAndKeepModuleWithStateInstalled() {
        // Given
        Module inputModule = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();

        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(INSTALLED);
    }

    @Test
    void shouldNotBuildModuleAndKeepModuleWithStateError() {
        // Given
        Module inputModule = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();
        inputModule.error(new ESBException("Module in error state!"));

        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(ERROR);
    }

    @Test
    void shouldNotBuildModuleAndKeepModuleWithStateUnresolved() {
        // Given
        Module inputModule = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();
        inputModule.unresolve(unresolvedComponents, resolvedComponents);

        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(UNRESOLVED);
    }

    @Test
    void shouldNotBuildModuleAndKeepModuleWithStateStopped() {
        // Given
        Module inputModule = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();
        inputModule.unresolve(unresolvedComponents, resolvedComponents);
        inputModule.resolve(resolvedComponents);
        inputModule.stop(ImmutableList.of(flow));

        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(STOPPED);
    }

    @Test
    void shouldNotBuildModuleAndKeepModuleWithStateStarted() {
        // Given
        Module inputModule = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();
        inputModule.unresolve(unresolvedComponents, resolvedComponents);
        inputModule.resolve(resolvedComponents);
        inputModule.stop(ImmutableList.of(flow));
        inputModule.start(ImmutableList.of(flow));

        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(STARTED);
    }

    @Test
    void shouldBuildModuleWhenStateIsResolvedAndTransitionToStopped() {
        // Given
        Module inputModule = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();
        inputModule.unresolve(unresolvedComponents, resolvedComponents);
        inputModule.resolve(resolvedComponents);

        JSONObject flowDefinition = parseFlow(TestFlow.WITH_SOME_COMPONENTS);
        Set<JSONObject> flows = new HashSet<>();
        flows.add(flowDefinition);

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule)
                .when(step)
                .deserializedModule(bundle);

        mockComponentWithServiceReference(TestInboundComponent.class);
        mockComponentWithServiceReference(TestComponent.class);
        mockComponent(Stop.class);

        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(STOPPED);

        Collection<Flow> constructedFlows = module.flows();
        assertThat(constructedFlows).hasSize(1);

        Flow next = module.flows().iterator().next();
        assertThat(next.getFlowId()).isEqualTo("45a5ce60-5c9d-4075-82ab-d3fa9284f52a");
        assertThat(next.isStarted()).isFalse();
    }

    @Test
    void shouldTransitionToErrorStateWhenJsonIsNotDeserializable() {
        // Given
        Module inputModule = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();
        inputModule.unresolve(unresolvedComponents, resolvedComponents);
        inputModule.resolve(resolvedComponents);

        doThrow(new JSONException("JSON could not be parsed"))
                .when(step)
                .deserializedModule(bundle);

        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(ERROR);

        Collection<Exception> errors = module.errors();
        assertThat(errors).hasSize(1);

        Exception exception = errors.iterator().next();
        assertThat(exception.getMessage()).isEqualTo("JSON could not be parsed");
    }

    @Test
    void shouldTransitionToErrorStateWhenFlowDoesNotContainAnId() {
        // Given
        Module inputModule = newResolvedModule();

        JSONObject flowWithoutId = parseFlow(TestFlow.WITHOUT_ID);
        Set<JSONObject> flows = new HashSet<>();
        flows.add(flowWithoutId);

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule)
                .when(step)
                .deserializedModule(bundle);

        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(ERROR);

        Collection<Exception> errors = module.errors();
        assertThat(errors).hasSize(1);

        Exception exception = errors.iterator().next();
        assertThat(exception.getMessage()).isEqualTo("\"id\" property must be defined in the flow definition");
    }

    @Test
    void shouldTransitionToErrorStateAndListAllExceptionFromFlowConstruction() {
        // Given
        Module inputModule = newResolvedModule();

        JSONObject flowWithoutId = parseFlow(TestFlow.WITHOUT_ID);
        JSONObject flowWithNotWellFormedChoice = parseFlow(TestFlow.WITH_NOT_WELL_FORMED_CHOICE);
        Set<JSONObject> flows = new HashSet<>();
        flows.add(flowWithoutId);
        flows.add(flowWithNotWellFormedChoice);

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule)
                .when(step)
                .deserializedModule(bundle);

        mockComponentWithServiceReference(TestInboundComponent.class);
        mockComponentWithServiceReference(AnotherTestComponent.class);
        mockComponent(Choice.class);
        mockComponent(Stop.class);

        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(ERROR);

        Collection<Exception> errors = module.errors();
        assertThat(errors).hasSize(2);

        assertThatExistsExceptionWithMessage(errors, "\"id\" property must be defined in the flow definition");
        assertThatExistsExceptionWithMessage(errors, "JSONObject[\"otherwise\"] not found.");
    }

    @Test
    void shouldTransitionToErrorStateWhenThereAreTwoFlowsWithSameId() {
        // Given
        Module inputModule = newResolvedModule();

        JSONObject flowWithId = parseFlow(TestFlow.WITH_SOME_COMPONENTS);
        JSONObject flowWithSameId = parseFlow(TestFlow.WITH_SOME_COMPONENTS);
        Set<JSONObject> flows = new HashSet<>();
        flows.add(flowWithId);
        flows.add(flowWithSameId);

        mockComponentWithServiceReference(TestInboundComponent.class);
        mockComponentWithServiceReference(TestComponent.class);
        mockComponent(Stop.class);

        DeserializedModule deserializedModule = new DeserializedModule(flows, emptySet(), emptySet());
        doReturn(deserializedModule)
                .when(step)
                .deserializedModule(bundle);


        // When
        Module module = step.run(inputModule);

        // Then
        assertThat(module.state()).isEqualTo(ERROR);

        Collection<Exception> errors = module.errors();
        assertThat(errors).hasSize(1);

        assertThatExistsExceptionWithMessage(errors, "There are at least two flows with the same id. Flows Ids must be unique.");
    }

    private <T extends Component> void mockComponentWithServiceReference(Class<T> clazz) {
        try {
            T component = clazz.getConstructor().newInstance();
            ExecutionNode componentExecutionNode = new ExecutionNode(new ReferencePair<>(component, serviceReference));
            mockInstantiateComponent(componentExecutionNode, clazz);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            fail("mockComponentWithServiceReference", e);
        }
    }

    private <T extends Component> void mockComponent(Class<T> clazz) {
        try {
            T component = clazz.getConstructor().newInstance();
            ExecutionNode componentExecutionNode = new ExecutionNode(new ReferencePair<>(component));
            mockInstantiateComponent(componentExecutionNode, clazz);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            fail("mockComponent", e);
        }
    }

    private void mockInstantiateComponent(ExecutionNode componentExecutionNode, Class clazz) {
        doReturn(componentExecutionNode)
                .when(modulesManager)
                .instantiateComponent(bundleContext, clazz.getName());

        if (componentExecutionNode.getComponentReference().getServiceReference() != null) {
            doReturn(serviceObjects)
                    .when(bundleContext)
                    .getServiceObjects(serviceReference);
        }
    }

    private void assertThatExistsExceptionWithMessage(Collection<Exception> errors, String expectedMessage) {
        Iterator<Exception> it = errors.iterator();
        boolean found = false;
        while (it.hasNext()) {
            Exception next = it.next();
            if (expectedMessage.equals(next.getMessage())) {
                found = true;
            }
        }
        assertThat(found).isTrue();
    }

    private Module newResolvedModule() {
        Module inputModule = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();
        inputModule.unresolve(unresolvedComponents, resolvedComponents);
        inputModule.resolve(resolvedComponents);
        return inputModule;
    }

}