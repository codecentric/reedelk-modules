package com.esb.flow;

import com.esb.api.component.Component;
import com.esb.api.component.Implementor;
import com.esb.graph.ExecutionNode;
import com.esb.graph.ExecutionNode.ReferencePair;
import com.esb.test.utils.TestComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.ServiceReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExecutionNodeTest {

    @Mock
    private ServiceReference<Component> serviceReference;

    private TestComponent testComponent;

    @BeforeEach
    void setUp() {
        testComponent = new TestComponent();
    }

    @Test
    void shouldReturnCorrectComponentReference() {
        // Given
        ReferencePair<Component> expectedReference = new ReferencePair<>(testComponent, serviceReference);
        ExecutionNode EN = new ExecutionNode(expectedReference);

        // When
        ReferencePair<Component> actualReference = EN.getComponentReference();

        // Then
        assertThat(actualReference).isEqualTo(expectedReference);
    }

    @Test
    void shouldAddDependencyReferenceCorrectly() {
        // Given
        ExecutionNode testComponentEN = mockExecutionNodeWithComponentAndReference(testComponent, serviceReference);

        ReferencePair<Implementor> referencePairDependency1 = mockReferencePair(new Dependency());
        testComponentEN.add(referencePairDependency1);

        ReferencePair<Implementor> referencePairDependency2 = mockReferencePair(new Dependency());
        testComponentEN.add(referencePairDependency2);

        // When
        List<ReferencePair<Implementor>> actualDependencyReferences = testComponentEN.getDependencyReferences();

        // Then
        assertThat(actualDependencyReferences)
                .containsExactlyInAnyOrder(referencePairDependency1, referencePairDependency2);
    }

    @Test
    void shouldReturnCorrectComponent() {
        // Given
        ExecutionNode testComponentEN = mockExecutionNodeWithComponentAndReference(testComponent, serviceReference);

        // When
        Component actualComponent = testComponentEN.getComponent();

        // Then
        assertThat(actualComponent).isEqualTo(testComponent);
    }

    @Test
    void shouldClearReferencesRemoveAllReferences() {
        // Given
        ExecutionNode testComponentEN = mockExecutionNodeWithComponentAndReference(testComponent, serviceReference);

        ReferencePair<Implementor> dependencyReferencePair1 = mockReferencePair(new Dependency());
        testComponentEN.add(dependencyReferencePair1);

        ReferencePair<Implementor> dependencyReferencePair2 = mockReferencePair(new Dependency());
        testComponentEN.add(dependencyReferencePair2);

        // When
        testComponentEN.clearReferences();

        // Then
        assertThat(testComponentEN.getComponentReference().getImplementor()).isNull();
        assertThat(testComponentEN.getComponentReference().getServiceReference()).isNull();

        assertThat(testComponentEN.getDependencyReferences()).isEmpty();
    }

    @Test
    void shouldClearReferencesClearReferencesForDependencies() {
        // Given
        ExecutionNode testComponentEN = mockExecutionNodeWithComponentAndReference(testComponent, serviceReference);

        ReferencePair<Implementor> dependencyReferencePair1 = mockReferencePair(new Dependency());
        testComponentEN.add(dependencyReferencePair1);

        ReferencePair<Implementor> dependencyReferencePair2 = mockReferencePair(new Dependency());
        testComponentEN.add(dependencyReferencePair2);

        // When
        testComponentEN.clearReferences();

        // Then
        assertThat(dependencyReferencePair1.getImplementor()).isNull();
        assertThat(dependencyReferencePair1.getServiceReference()).isNull();
        assertThat(dependencyReferencePair2.getImplementor()).isNull();
        assertThat(dependencyReferencePair2.getServiceReference()).isNull();
    }

    @Test
    void shouldClearReferencesCallDisposeWhenImplementorIsAComponent() {
        // Given
        Component testComponentSpy = spy(testComponent);
        ExecutionNode testComponentEN = mockExecutionNodeWithComponentAndReference(testComponentSpy, serviceReference);

        // When
        testComponentEN.clearReferences();

        // Then
        verify(testComponentSpy).dispose();
    }

    @Test
    void shouldIsUsingComponentReturnTrueWhenExecutionNodeComponentMatches() {
        // Given
        ExecutionNode testComponentEN = mockExecutionNodeWithComponentAndReference(testComponent, serviceReference);

        // When
        boolean actualIsUsingComponent = testComponentEN.isUsingComponent(TestComponent.class.getName());

        // Then
        assertThat(actualIsUsingComponent).isTrue();
    }

    @Test
    void shouldIsUsingComponentReturnTrueWhenDependencyMatches() {
        // Given
        ExecutionNode testComponentEN = mockExecutionNodeWithComponentAndReference(testComponent, serviceReference);

        testComponentEN.add(mockReferencePair(new Dependency()));
        testComponentEN.add(mockReferencePair(new Dependency()));

        // When
        boolean actualIsUsingComponent = testComponentEN.isUsingComponent(Dependency.class.getName());

        // Then
        assertThat(actualIsUsingComponent).isTrue();
    }

    @Test
    void shouldIsUsingComponentReturnFalse() {
        // Given
        ExecutionNode testComponentEN = mockExecutionNodeWithComponentAndReference(testComponent, serviceReference);

        testComponentEN.add(mockReferencePair(new Dependency()));
        testComponentEN.add(mockReferencePair(new Dependency()));

        // When
        boolean actualIsUsingComponent = testComponentEN.isUsingComponent("com.not.existent.Component.Name");

        // Then
        assertThat(actualIsUsingComponent).isFalse();
    }

    private ReferencePair<Implementor> mockReferencePair(Implementor implementor) {
        ServiceReference<Implementor> dependencyServiceReference = mock(ServiceReference.class);
        return new ReferencePair<>(implementor, dependencyServiceReference);
    }


    private class Dependency implements Implementor {
    }

    private ExecutionNode mockExecutionNodeWithComponentAndReference(Component component, ServiceReference<Component> serviceReference) {
        return spy(new ExecutionNode(new ReferencePair<>(component, serviceReference)));
    }

}