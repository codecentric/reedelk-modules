package com.reedelk.esb.services.scriptengine.converter;

import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ValueConverterFactoryTest {

    @Nested
    @DisplayName("Convert object from input to output class")
    class ConvertObjectFromInputToOutputClass {

        // string

        @Test
        void shouldConvertStringToBoolean() {
            // Given
            String value = "true";

            // When
            Boolean actual = ValueConverterFactory.convert(value, Boolean.class);

            // Then
            assertThat(actual).isTrue();
        }

        @Test
        void shouldConvertStringToByteArray() {
            // Given
            String value = "Test text value";

            // When
            byte[] actual = ValueConverterFactory.convert(value, byte[].class);

            // Then
            assertThat(actual).isEqualTo(value.getBytes());
        }

        @Test
        void shouldConvertStringToDouble() {
            // Given
            String value = "234.21312";

            // When
            Double actual = ValueConverterFactory.convert(value, Double.class);

            // Then
            assertThat(actual).isEqualTo(Double.parseDouble(value));
        }

        @Test
        void shouldConvertStringToFloat() {
            // Given
            String value = "24.2341";

            // When
            Float actual = ValueConverterFactory.convert(value, Float.class);

            // Then
            assertThat(actual).isEqualTo(Float.parseFloat(value));
        }

        @Test
        void shouldConvertStringToInteger() {
            // Given
            String value = "234";

            // When
            Integer actual = ValueConverterFactory.convert(value, Integer.class);

            // Then
            assertThat(actual).isEqualTo(Integer.parseInt(value));
        }

        @Test
        void shouldConvertStringToString() {
            // Given
            String value = "Test text value";

            // When
            String result = ValueConverterFactory.convert(value, String.class);

            // Then
            assertThat(result).isEqualTo(value);
        }

        @Test
        void shouldReturnNullWhenValueToConvertIsNull() {
            // Given
            String value = null;

            // When
            String result = ValueConverterFactory.convert(value, String.class);

            // Then
            assertThat(result).isNull();
        }

        // boolean

        @Test
        void shouldConvertBooleanToString() {
            // Given
            boolean value = true;

            // When
            String result = ValueConverterFactory.convert(value, String.class);

            // Then
            assertThat(result).isEqualTo("true");
        }

        @Test
        void shouldConvertBooleanToInteger() {
            // Given
            boolean value = true;

            // When
            Integer result = ValueConverterFactory.convert(value, Integer.class);

            // Then
            assertThat(result).isEqualTo(1);
        }

        // byte array

        @Test
        void shouldConvertByteArrayToString() {
            // Given
            byte[] value = "my test".getBytes();

            // When
            String result = ValueConverterFactory.convert(value, String.class);

            // Then
            assertThat(result).isEqualTo("my test");
        }

        // double

        @Test
        void shouldConvertDoubleToFloat() {
            // Given
            Double value = 234.234d;

            // When
            Float result = ValueConverterFactory.convert(value,Float.class);

            // Then
            assertThat(result).isEqualTo(value.floatValue());
        }

        @Test
        void shouldConvertDoubleToInteger() {
            // Given
            Double value = 234.123;

            // When
            Integer result = ValueConverterFactory.convert(value, Integer.class);

            // Then
            assertThat(result).isEqualTo(value.intValue());
        }

        // exception

        @Test
        void shouldConvertExceptionToString() {
            // Given
            ESBException testException = new ESBException("an error");

            // When
            String result = ValueConverterFactory.convert(testException, ESBException.class, String.class);

            // Then
            assertThat(result).isEqualTo(StackTraceUtils.asString(testException));
        }

        @Test
        void shouldConvertExceptionToByteArray() {
            // Given
            ESBException testException = new ESBException("another error");

            // When
            byte[] result = ValueConverterFactory.convert(testException, ESBException.class, byte[].class);

            // Then
            assertThat(result).isEqualTo(StackTraceUtils.asByteArray(testException));
        }

        @Test
        void shouldThrowExceptionWhenConverterNotPresent() {
            // Given
            DummyClazz input = new DummyClazz();

            // When
            ESBException exception = Assertions.assertThrows(ESBException.class,
                    () -> ValueConverterFactory.convert(input, DummyClazz.class, Integer.class));

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception).hasMessage("Converter for input=[com.reedelk.esb.services.scriptengine.converter.ValueConverterFactoryTest$DummyClazz] to output=[java.lang.Integer] not available");
        }

        // integer

        @Test
        void shouldConvertIntegerToByteArray() {
            // Given
            Integer value = 234123;

            // When
            byte[] result = ValueConverterFactory.convert(value, byte[].class);

            // Then
            assertThat(result).isEqualTo(new byte[]{value.byteValue()});
        }

        @Test
        void shouldConvertIntegerToDouble() {
            // Given
            Integer value = 234;

            // When
            Double result = ValueConverterFactory.convert(value, Double.class);

            // Then
            assertThat(result).isEqualTo(value.doubleValue());
        }

        @Test
        void shouldConvertIntegerToFloat() {
            // Given
            Integer value = 865;

            // When
            Float result = ValueConverterFactory.convert(value, Float.class);

            // Then
            assertThat(result).isEqualTo(value.floatValue());
        }

        @Test
        void shouldConvertIntegerToString() {
            // Given
            Integer value = 234;

            // When
            String result = ValueConverterFactory.convert(value, String.class);

            // Then
            assertThat(result).isEqualTo("234");
        }

        // object

        @Test
        void shouldReturnOriginalValueWhenOutputClazzIsObject() {
            // Given
            BigDecimal aBigDecimal = new BigDecimal(234);

            // When
            Object result = ValueConverterFactory.convert(aBigDecimal, BigDecimal.class, Object.class);

            // Then
            assertThat(result).isEqualTo(aBigDecimal);
        }
    }

    @Nested
    @DisplayName("Convert typed publisher")
    class ConvertTypedPublisher {

        @Test
        void shouldConvertStringToInteger() {
            // Given
            TypedPublisher<String> input = TypedPublisher.fromString(Flux.just("1", "2", "4"));

            // When
            TypedPublisher<Integer> converted =
                    ValueConverterFactory.convertTypedPublisher(input, Integer.class);

            // Then
            StepVerifier.create(converted)
                    .expectNext(1, 2, 4)
                    .verifyComplete();
        }

        @Test
        void shouldStreamPropagateErrorWhenConversionIsFailed() {
            // Given
            TypedPublisher<String> input = TypedPublisher.fromString(Flux.just("1", "not a number", "2"));

            // When
            Publisher<Integer> converted =
                    ValueConverterFactory.convertTypedPublisher(input, Integer.class);

            // Then
            StepVerifier.create(converted)
                    .expectNext(1)
                    .expectError()
                    .verify();
        }

        @Test
        void shouldReturnNullWhenPublisherIsNull() {
            // Given
            TypedPublisher<Integer> input = null;

            // When
            Publisher<Integer> converted =
                    ValueConverterFactory.convertTypedPublisher(input, Integer.class);

            // Then
            assertThat(converted).isNull();
        }

        @Test
        void shouldReturnOriginalPublisherIfPublisherTypeEqualsOutputType() {
            // Given
            TypedPublisher<Integer> input = TypedPublisher.fromInteger(Flux.just(3, 4, 20));

            // When
            TypedPublisher<Integer> result =
                    ValueConverterFactory.convertTypedPublisher(input, Integer.class);

            // Then
            assertThat(result).isEqualTo(input);
        }

        @Test
        void shouldThrowExceptionWhenConverterNotPresent() {
            // Given
            TypedPublisher<DummyClazz> input = TypedPublisher.from(Flux.just(new DummyClazz()), DummyClazz.class);

            // When
            ESBException exception = Assertions.assertThrows(ESBException.class,
                    () -> ValueConverterFactory.convertTypedPublisher(input, Integer.class));

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception).hasMessage("Converter for input=[com.reedelk.esb.services.scriptengine.converter.ValueConverterFactoryTest$DummyClazz] to output=[java.lang.Integer] not available");
        }
    }

    private static class DummyClazz{}
}