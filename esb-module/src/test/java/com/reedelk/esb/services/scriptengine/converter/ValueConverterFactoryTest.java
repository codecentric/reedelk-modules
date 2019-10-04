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

        @Test
        void shouldConvertStringToByteArray() {
            // Given
            String myString = "Test text value";

            // When
            byte[] result = ValueConverterFactory.convert(myString, String.class, byte[].class);

            // Then
            assertThat(result).isEqualTo(myString.getBytes());
        }

        @Test
        void shouldConvertStringToString() {
            // Given
            String myString = "Test text value";

            // When
            String result = ValueConverterFactory.convert(myString, String.class, String.class);

            // Then
            assertThat(result).isEqualTo(myString);
        }

        @Test
        void shouldReturnNullWhenValueToConvertIsNull() {
            // Given
            Integer myValue = null;

            // When
            String result = ValueConverterFactory.convert(myValue, Integer.class, String.class);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void shouldReturnOriginalValueWhenOutputClazzIsObject() {
            // Given
            BigDecimal aBigDecimal = new BigDecimal(234);

            // When
            Object result = ValueConverterFactory.convert(aBigDecimal, BigDecimal.class, Object.class);

            // Then
            assertThat(result).isEqualTo(aBigDecimal);
        }

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