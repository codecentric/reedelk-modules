    package com.reedelk.esb.services.scriptengine.converter;

    import com.reedelk.runtime.api.message.type.TypedPublisher;
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Nested;
    import org.junit.jupiter.api.Test;
    import org.reactivestreams.Publisher;
    import reactor.core.publisher.Flux;
    import reactor.test.StepVerifier;

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
    }

    @Nested
    @DisplayName("Convert typed publisher")
    class ConvertTypedPublisher {

        @Test
        void shouldConvertStringToInteger() {
            // Given
            TypedPublisher<String> input = TypedPublisher.fromString(Flux.just("1","2","4"));

            // When
            TypedPublisher<Integer> converted =
                    ValueConverterFactory.convertTypedPublisher(input, Integer.class);

            // Then
            StepVerifier.create(converted)
                    .expectNext(1,2, 4)
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

    }
}