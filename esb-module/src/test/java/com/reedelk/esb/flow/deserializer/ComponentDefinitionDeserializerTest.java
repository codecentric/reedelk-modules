package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.test.utils.*;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.component.Implementor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComponentDefinitionDeserializerTest {

    @Mock
    private ExecutionNode mockExecutionNode;
    @Spy
    private MockFlowBuilderContext context;

    private ComponentDefinitionDeserializer deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new ComponentDefinitionDeserializer(mockExecutionNode, context);
    }

    @Nested
    @DisplayName("Primitive type")
    class PrimitiveTypeTests {

        @Test
        void shouldCorrectlySetStringProperty() {
            // Given
            String expectedValue = "Hello";
            TestComponent component = buildComponentWith("stringProperty", expectedValue);

            // Then
            assertThat(component.getStringProperty()).isEqualTo(expectedValue);
        }

        @Test
        void shouldCorrectlySetNullStringProperty() {
            // Given
            TestComponent component = buildComponentWith("stringProperty", null);

            // Then
            assertThat(component.getStringProperty()).isNull();
        }

        @Test
        void shouldCorrectlySetLongProperty() {
            // Given
            Object expectedValue = 234;
            TestComponent component = buildComponentWith("longProperty", expectedValue);

            // Then
            assertThat(component.getLongProperty()).isEqualTo(Long.valueOf(234));
        }

        @Test
        void shouldCorrectlySetDefaultWhenNullLongProperty() {
            // Given
            long value = 0L;
            TestComponent component = buildComponentWith("longProperty", null);

            // Then
            assertThat(component.getLongProperty()).isEqualTo(value);
        }

        @Test
        void shouldCorrectlySetLongObjectProperty() {
            // Given
            Object expectedValue = 5432;
            TestComponent component = buildComponentWith("longObjectProperty", expectedValue);

            // Then
            assertThat(component.getLongObjectProperty()).isEqualTo(Long.valueOf(5432));
        }

        @Test
        void shouldCorrectlySetNullLongObjectProperty() {
            // Given
            TestComponent component = buildComponentWith("longObjectProperty", null);

            // Then
            assertThat(component.getLongObjectProperty()).isNull();
        }

        @Test
        void shouldCorrectlySetIntProperty() {
            // Given
            Object expectedValue = 20;
            TestComponent component = buildComponentWith("intProperty", expectedValue);

            // Then
            assertThat(component.getIntProperty()).isEqualTo(Integer.valueOf(20));
        }

        @Test
        void shouldCorrectlySetDefaultWhenNullIntProperty() {
            // Given
            int value = 0;
            TestComponent component = buildComponentWith("intProperty", null);

            // Then
            assertThat(component.getIntProperty()).isEqualTo(value);
        }

        @Test
        void shouldCorrectlySetIntObjectProperty() {
            // Given
            Object expectedValue = 20;
            TestComponent component = buildComponentWith("intObjectProperty", expectedValue);

            // Then
            assertThat(component.getIntObjectProperty()).isEqualTo(Integer.valueOf(20));
        }

        @Test
        void shouldCorrectlySetNullIntObjectProperty() {
            // Given
            TestComponent component = buildComponentWith("intObjectProperty", null);

            // Then
            assertThat(component.getIntObjectProperty()).isNull();
        }

        @Test
        void shouldCorrectlySetDoubleProperty() {
            // Given
            Object expectedValue = 234.3;
            TestComponent component = buildComponentWith("doubleProperty", expectedValue);

            // Then
            assertThat(component.getDoubleProperty()).isEqualTo(Double.valueOf(234.3d));
        }

        @Test
        void shouldCorrectlySetDefaultWhenNullDoubleProperty() {
            // Given
            TestComponent component = buildComponentWith("doubleProperty", null);

            // Then
            assertThat(component.getDoubleProperty()).isEqualTo(Double.valueOf(0.0d));
        }

        @Test
        void shouldCorrectlySetDoubleObjectProperty() {
            // Given
            Object expectedValue = 111.432;
            TestComponent component = buildComponentWith("doubleObjectProperty", expectedValue);

            // Then
            assertThat(component.getDoubleObjectProperty()).isEqualTo(Double.valueOf(111.432));
        }

        @Test
        void shouldCorrectlySetNullDoubleObjectProperty() {
            // Given
            TestComponent component = buildComponentWith("doubleObjectProperty", null);

            // Then
            assertThat(component.getDoubleObjectProperty()).isNull();
        }

        @Test
        void shouldCorrectlySetFloatProperty() {
            // Given
            Object expectedValue = 234.2f;
            TestComponent component = buildComponentWith("floatProperty", expectedValue);

            // Then
            assertThat(component.getFloatProperty()).isEqualTo(Float.valueOf(234.2f));
        }

        @Test
        void shouldCorrectlySetDefaultWhenNullFloatProperty() {
            // Given
            TestComponent component = buildComponentWith("floatProperty", null);

            // Then
            assertThat(component.getFloatProperty()).isEqualTo(Float.valueOf(0.0f));
        }

        @Test
        void shouldCorrectlySetFloatObjectProperty() {
            // Given
            Object expectedValue = Float.MAX_VALUE;
            TestComponent component = buildComponentWith("floatObjectProperty", expectedValue);

            // Then
            assertThat(component.getFloatObjectProperty()).isEqualTo(Float.valueOf(Float.MAX_VALUE));
        }

        @Test
        void shouldCorrectlySetNullFloatObjectProperty() {
            // Given
            TestComponent component = buildComponentWith("floatObjectProperty", null);

            // Then
            assertThat(component.getFloatObjectProperty()).isNull();
        }

        @Test
        void shouldCorrectlySetBooleanProperty() {
            // Given
            Object expectedValue = true;
            TestComponent component = buildComponentWith("booleanProperty", expectedValue);

            // Then
            assertThat(component.isBooleanProperty()).isTrue();
        }

        @Test
        void shouldCorrectlySetDefaultWhenNullBooleanProperty() {
            // Given
            TestComponent component = buildComponentWith("booleanProperty", null);

            // Then
            assertThat(component.isBooleanProperty()).isFalse();
        }

        @Test
        void shouldCorrectlySetBooleanObjectProperty() {
            // Given
            Object expectedValue = true;
            TestComponent component = buildComponentWith("booleanObjectProperty", expectedValue);

            // Then
            assertThat(component.getBooleanObjectProperty()).isTrue();
        }

        @Test
        void shouldCorrectlySetNullBooleanObjectProperty() {
            // Given
            TestComponent component = buildComponentWith("booleanObjectProperty", null);

            // Then
            assertThat(component.getBooleanObjectProperty()).isNull();
        }

        @Test
        void shouldCorrectlySetNumberProperty() {
            // Given
            Object expectedValue = 24234.23;
            TestComponent component = buildComponentWith("numberProperty", expectedValue);

            // Then
            assertThat(component.getNumberProperty()).isEqualTo(24234.23d);
        }

        @Test
        void shouldCorrectlySetNullNumberProperty() {
            // Given
            TestComponent component = buildComponentWith("numberProperty", null);

            // Then
            assertThat(component.getNumberProperty()).isNull();
        }

        @Test
        void shouldCorrectlySetBigDecimalProperty() {
            // Given
            Object expectedValue = 2343;
            TestComponent component = buildComponentWith("bigDecimalProperty", expectedValue);

            // Then
            assertThat(component.getBigDecimalProperty()).isEqualTo(new BigDecimal("2343"));
        }

        @Test
        void shouldCorrectlySetNullBigDecimalProperty() {
            // Given
            TestComponent component = buildComponentWith("bigDecimalProperty", null);

            // Then
            assertThat(component.getBigDecimalProperty()).isNull();
        }

        @Test
        void shouldCorrectlySetBigIntegerProperty() {
            // Given
            Object expectedValue = Integer.MAX_VALUE;
            TestComponent component = buildComponentWith("bigIntegerProperty", expectedValue);

            // Then
            assertThat(component.getBigIntegerProperty()).isEqualTo(new BigInteger(String.valueOf(Integer.MAX_VALUE)));
        }

        @Test
        void shouldCorrectlySetNullBigIntegerProperty() {
            // Given
            TestComponent component = buildComponentWith("bigIntegerProperty", null);

            // Then
            assertThat(component.getBigIntegerProperty()).isNull();
        }
    }

    @Nested
    @DisplayName("Collection/s properties")
    class CollectionTests {

        @Test
        void shouldCorrectlySetLongCollection() {
            // Given
            JSONArray array = newArray(21233, Long.MAX_VALUE, Long.MIN_VALUE);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myLongCollection", array);

            // Then
            Collection<Long> collection = component.getMyLongCollection();
            assertAllItemsOfType(collection, Long.class);
            assertThat(collection).containsExactlyInAnyOrder(21233L, Long.MAX_VALUE, Long.MIN_VALUE);
        }

        @Test
        void shouldCorrectlySetIntCollection() {
            // Given
            JSONArray array = newArray(23, Integer.MAX_VALUE, Integer.MIN_VALUE);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myIntCollection", array);

            // Then
            Collection<Integer> collection = component.getMyIntCollection();
            assertAllItemsOfType(collection, Integer.class);
            assertThat(collection).containsExactlyInAnyOrder(23, Integer.MAX_VALUE, Integer.MIN_VALUE);
        }

        @Test
        void shouldCorrectlySetFloatCollection() {
            // Given
            JSONArray array = newArray(23.1f, Float.MAX_VALUE, 234.15f);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myFloatCollection", array);

            // Then
            Collection<Float> collection = component.getMyFloatCollection();
            assertAllItemsOfType(collection, Float.class);
            assertThat(collection).containsExactlyInAnyOrder(23.1f, Float.MAX_VALUE, 234.15f);
        }

        @Test
        void shouldCorrectlySetDoubleCollection() {
            // Given
            JSONArray array = newArray(234.234d, Double.MIN_VALUE, 1.234d);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myDoubleCollection", array);

            // Then
            Collection<Double> collection = component.getMyDoubleCollection();
            assertAllItemsOfType(collection, Double.class);
            assertThat(collection).containsExactlyInAnyOrder(234.234d, Double.MIN_VALUE, 1.234d);
        }

        @Test
        void shouldCorrectlySetStringCollection() {
            // Given
            JSONArray array = newArray("Item1", "Item2", "Item3");
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myStringCollection", array);

            // Then
            Collection<String> collection = component.getMyStringCollection();
            assertAllItemsOfType(collection, String.class);
            assertThat(collection).containsExactlyInAnyOrder("Item1", "Item2", "Item3");
        }


        @Test
        void shouldCorrectlySetBooleanCollection() {
            // Given
            JSONArray array = newArray(true, false, true);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myBooleanCollection", array);

            // Then
            Collection<Boolean> collection = component.getMyBooleanCollection();
            assertAllItemsOfType(collection, Boolean.class);
            assertThat(collection).containsExactlyInAnyOrder(true, false, true);
        }

        @Test
        void shouldCorrectlySetNumberCollection() {
            // Given
            JSONArray array = newArray(234, 234.23f, 5.12324d);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myNumberCollection", array);

            // Then
            Collection<Number> collection = component.getMyNumberCollection();
            assertAllItemsOfType(collection, Number.class);
            assertThat(collection).containsExactlyInAnyOrder(234, 234.23f, 5.12324d);
        }

        @Test
        void shouldCorrectlySetBigIntegerCollection() {
            // Given
            JSONArray array = newArray(243234324, 77465);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myBigIntegerCollection", array);

            // Then
            Collection<BigInteger> collection = component.getMyBigIntegerCollection();
            assertAllItemsOfType(collection, BigInteger.class);
            assertThat(collection).containsExactlyInAnyOrder(new BigInteger("243234324"), new BigInteger("77465"));
        }

        @Test
        void shouldCorrectlySetBigDecimalCollection() {
            // Given
            JSONArray array = newArray(99988, 777788);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myBigDecimalCollection", array);

            // Then
            Collection<BigDecimal> collection = component.getMyBigDecimalCollection();
            assertAllItemsOfType(collection, BigDecimal.class);
            assertThat(collection).containsExactlyInAnyOrder(new BigDecimal("99988"), new BigDecimal("777788"));
        }
    }

    @Nested
    @DisplayName("List/s properties")
    class ListTests {

        @Test
        void shouldCorrectlySetLongList() {
            // Given
            JSONArray array = newArray(21233, Long.MAX_VALUE, Long.MIN_VALUE);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myLongList", array);

            // Then
            List<Long> collection = component.getMyLongList();
            assertAllItemsOfType(collection, Long.class);
            assertThat(collection).containsExactlyInAnyOrder(21233L, Long.MAX_VALUE, Long.MIN_VALUE);
        }

        @Test
        void shouldCorrectlySetIntList() {
            // Given
            JSONArray array = newArray(23, Integer.MAX_VALUE, Integer.MIN_VALUE);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myIntList", array);

            // Then
            List<Integer> collection = component.getMyIntList();
            assertAllItemsOfType(collection, Integer.class);
            assertThat(collection).containsExactlyInAnyOrder(23, Integer.MAX_VALUE, Integer.MIN_VALUE);
        }

        @Test
        void shouldCorrectlySetFloatList() {
            // Given
            JSONArray array = newArray(23.1f, Float.MAX_VALUE, 234.15f);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myFloatList", array);

            // Then
            List<Float> collection = component.getMyFloatList();
            assertAllItemsOfType(collection, Float.class);
            assertThat(collection).containsExactlyInAnyOrder(23.1f, Float.MAX_VALUE, 234.15f);
        }

        @Test
        void shouldCorrectlySetDoubleList() {
            // Given
            JSONArray array = newArray(234.234d, Double.MIN_VALUE, 1.234d);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myDoubleList", array);

            // Then
            List<Double> collection = component.getMyDoubleList();
            assertAllItemsOfType(collection, Double.class);
            assertThat(collection).containsExactlyInAnyOrder(234.234d, Double.MIN_VALUE, 1.234d);
        }

        @Test
        void shouldCorrectlySetStringList() {
            // Given
            JSONArray array = newArray("Item1", "Item2", "Item3");
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myStringList", array);

            // Then
            List<String> collection = component.getMyStringList();
            assertAllItemsOfType(collection, String.class);
            assertThat(collection).containsExactlyInAnyOrder("Item1", "Item2", "Item3");
        }


        @Test
        void shouldCorrectlySetBooleanList() {
            // Given
            JSONArray array = newArray(true, false, true);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myBooleanList", array);

            // Then
            List<Boolean> collection = component.getMyBooleanList();
            assertAllItemsOfType(collection, Boolean.class);
            assertThat(collection).containsExactlyInAnyOrder(true, false, true);
        }

        @Test
        void shouldCorrectlySetNumberList() {
            // Given
            JSONArray array = newArray(234, 234.23f, 5.12324d);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myNumberList", array);

            // Then
            List<Number> collection = component.getMyNumberList();
            assertAllItemsOfType(collection, Number.class);
            assertThat(collection).containsExactlyInAnyOrder(234, 234.23f, 5.12324d);
        }

        @Test
        void shouldCorrectlySetBigIntegerList() {
            // Given
            JSONArray array = newArray(243234324, 77465);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myBigIntegerList", array);

            // Then
            List<BigInteger> collection = component.getMyBigIntegerList();
            assertAllItemsOfType(collection, BigInteger.class);
            assertThat(collection).containsExactlyInAnyOrder(new BigInteger("243234324"), new BigInteger("77465"));
        }

        @Test
        void shouldCorrectlySetBigDecimalList() {
            // Given
            JSONArray array = newArray(99988, 777788);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myBigDecimalList", array);

            // Then
            List<BigDecimal> collection = component.getMyBigDecimalList();
            assertAllItemsOfType(collection, BigDecimal.class);
            assertThat(collection).containsExactlyInAnyOrder(new BigDecimal("99988"), new BigDecimal("777788"));
        }

    }

    @Nested
    @DisplayName("Set/s properties")
    class SetTests {

        @Test
        void shouldCorrectlySetLongSet() {
            // Given
            JSONArray array = newArray(21233, Long.MAX_VALUE, Long.MIN_VALUE);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myLongSet", array);

            // Then
            Set<Long> collection = component.getMyLongSet();
            assertAllItemsOfType(collection, Long.class);
            assertThat(collection).containsExactlyInAnyOrder(21233L, Long.MAX_VALUE, Long.MIN_VALUE);
        }

        @Test
        void shouldCorrectlySetIntSet() {
            // Given
            JSONArray array = newArray(23, Integer.MAX_VALUE, Integer.MIN_VALUE);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myIntSet", array);

            // Then
            Set<Integer> collection = component.getMyIntSet();
            assertAllItemsOfType(collection, Integer.class);
            assertThat(collection).containsExactlyInAnyOrder(23, Integer.MAX_VALUE, Integer.MIN_VALUE);
        }

        @Test
        void shouldCorrectlySetFloatSet() {
            // Given
            JSONArray array = newArray(23.1f, Float.MAX_VALUE, 234.15f);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myFloatSet", array);

            // Then
            Set<Float> collection = component.getMyFloatSet();
            assertAllItemsOfType(collection, Float.class);
            assertThat(collection).containsExactlyInAnyOrder(23.1f, Float.MAX_VALUE, 234.15f);
        }

        @Test
        void shouldCorrectlySetDoubleSet() {
            // Given
            JSONArray array = newArray(234.234d, Double.MIN_VALUE, 1.234d, Double.MIN_VALUE);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myDoubleSet", array);

            // Then
            Set<Double> collection = component.getMyDoubleSet();
            assertAllItemsOfType(collection, Double.class);
            assertThat(collection).containsExactlyInAnyOrder(234.234d, Double.MIN_VALUE, 1.234d);
        }

        @Test
        void shouldCorrectlySetStringSet() {
            // Given
            JSONArray array = newArray("Item1", "Item2", "Item3", "Item3");
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myStringSet", array);

            // Then
            Set<String> collection = component.getMyStringSet();
            assertAllItemsOfType(collection, String.class);
            assertThat(collection).containsExactlyInAnyOrder("Item1", "Item2", "Item3");
        }


        @Test
        void shouldCorrectlySetBooleanSet() {
            // Given
            JSONArray array = newArray(true, false, true);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myBooleanSet", array);

            // Then
            Set<Boolean> collection = component.getMyBooleanSet();
            assertAllItemsOfType(collection, Boolean.class);
            assertThat(collection).containsExactlyInAnyOrder(true, false);
        }

        @Test
        void shouldCorrectlySetNumberSet() {
            // Given
            JSONArray array = newArray(234, 234.23f, 5.12324d);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myNumberSet", array);

            // Then
            Set<Number> collection = component.getMyNumberSet();
            assertAllItemsOfType(collection, Number.class);
            assertThat(collection).containsExactlyInAnyOrder(234, 234.23f, 5.12324d);
        }

        @Test
        void shouldCorrectlySetBigIntegerSet() {
            // Given
            JSONArray array = newArray(243234324, 77465);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myBigIntegerSet", array);

            // Then
            Set<BigInteger> collection = component.getMyBigIntegerSet();
            assertAllItemsOfType(collection, BigInteger.class);
            assertThat(collection).containsExactlyInAnyOrder(new BigInteger("243234324"), new BigInteger("77465"));
        }

        @Test
        void shouldCorrectlySetBigDecimalSet() {
            // Given
            JSONArray array = newArray(99988, 777788);
            TestComponentWithCollectionProperties component =
                    buildCollectionComponentWith("myBigDecimalSet", array);

            // Then
            Set<BigDecimal> collection = component.getMyBigDecimalSet();
            assertAllItemsOfType(collection, BigDecimal.class);
            assertThat(collection).containsExactlyInAnyOrder(new BigDecimal("99988"), new BigDecimal("777788"));
        }

    }

    @Nested
    @DisplayName("Objects tests")
    class ObjectTest {

        @Test
        void shouldCorrectlyMapJSONObjectToMap() {
            // Given
            JSONObject nestedObject = new JSONObject();
            nestedObject.put("property1", 23);
            nestedObject.put("property2", "Property2");
            nestedObject.put("property3", 234.02);
            JSONObject componentDefinition = ComponentsBuilder.forComponent(TestComponentWithMapProperty.class)
                    .with("myObjectProperty", nestedObject)
                    .build();

            mockComponent(TestComponentWithMapProperty.class);

            // When
            deserializer.deserialize(componentDefinition, mockExecutionNode.getComponent());

            // Then
            TestComponentWithMapProperty component = (TestComponentWithMapProperty) mockExecutionNode.getComponent();
            Map<String, Object> mappedMap = component.getMyObjectProperty();
            assertThat(mappedMap).isNotNull();
            assertThat(mappedMap).hasSize(3);
            assertThat(mappedMap.get("property1")).isEqualTo(23);
            assertThat(mappedMap.get("property2")).isEqualTo("Property2");
            assertThat(mappedMap.get("property3")).isEqualTo(234.02);
        }

        @Test
        void shouldCorrectlyMapJSONObjectToImplementor() {
            // Given
            JSONObject testImplementor = ComponentsBuilder.forComponent(TestImplementor.class)
                    .with("property1", "Test")
                    .with("property2", 23432434)
                    .build();

            JSONObject componentDefinition = ComponentsBuilder.forComponent(TestComponentWithObjectProperty.class)
                    .with("config", testImplementor)
                    .build();

            mockComponent(TestComponentWithObjectProperty.class);
            mockImplementor(TestImplementor.class);

            // When
            deserializer.deserialize(componentDefinition, mockExecutionNode.getComponent());

            // Then
            TestComponentWithObjectProperty component = (TestComponentWithObjectProperty) mockExecutionNode.getComponent();
            TestImplementor config = component.getConfig();
            assertThat(config).isNotNull();
            assertThat(config.getProperty1()).isEqualTo("Test");
            assertThat(config.getProperty2()).isEqualTo(new BigDecimal("23432434"));
        }

    }

    private TestComponent buildComponentWith(String propertyName, Object propertyValue) {
        // Given
        JSONObject definition = componentDefinitionWith(propertyName, propertyValue);

        // When
        TestComponent implementor = new TestComponent();
        deserializer.deserialize(definition, implementor);
        return implementor;
    }

    private TestComponentWithCollectionProperties buildCollectionComponentWith(String propertyName, Object propertyValue) {
        // Given
        JSONObject definition = componentCollectionDefinitionWith(propertyName, propertyValue);

        // When
        TestComponentWithCollectionProperties implementor = new TestComponentWithCollectionProperties();
        deserializer.deserialize(definition, implementor);
        return implementor;
    }

    private JSONObject componentDefinitionWith(String propertyName, Object propertyValue) {
        JSONObject componentDefinition = ComponentsBuilder.forComponent(TestComponent.class)
                .with(propertyName, propertyValue)
                .build();
        mockComponent(TestComponent.class);
        return componentDefinition;
    }

    private JSONObject componentCollectionDefinitionWith(String propertyName, Object propertyValue) {
        JSONObject componentDefinition = ComponentsBuilder.forComponent(TestComponentWithCollectionProperties.class)
                .with(propertyName, propertyValue)
                .build();
        mockComponent(TestComponentWithCollectionProperties.class);
        return componentDefinition;
    }

    private JSONArray newArray(Object... values) {
        JSONArray array = new JSONArray();
        stream(values).forEach(array::put);
        return array;
    }

    private void assertAllItemsOfType(Collection<?> collection, Class<?> expectedType) {
        collection.forEach(item -> assertThat(item).isInstanceOf(expectedType));
    }

    private void mockComponent(Class<? extends Component> componentClass) {
        Component component = instantiate(componentClass);
        doReturn(component).when(mockExecutionNode).getComponent();
        doReturn(mockExecutionNode).when(context).instantiateComponent(componentClass);
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiate(Class<? extends Implementor> clazz) {
        try {
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private void mockImplementor(Class<? extends Implementor> clazz) {
        Implementor implementor = instantiate(clazz);
        doReturn(implementor).when(context).instantiateImplementor(mockExecutionNode, clazz.getName());
    }
}
