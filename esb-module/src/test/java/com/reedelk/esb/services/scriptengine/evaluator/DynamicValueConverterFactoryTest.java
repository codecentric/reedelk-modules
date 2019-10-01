    package com.reedelk.esb.services.scriptengine.evaluator;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class DynamicValueConverterFactoryTest {

    @Test
    void shouldConvertStringStreamToIntStream() {
        // Given
        Publisher<String> input = Flux.just("1","2","4");

        // When
        Publisher<Integer> converted =
                DynamicValueConverterFactory.convertStream(input, String.class, Integer.class);

        // Then
        StepVerifier.create(converted)
                .expectNext(1,2, 4)
                .verifyComplete();
    }

    @Test
    void shouldStreamPropagateErrorWhenConversionIsFailed() {
        // Given
        Publisher<String> input = Flux.just("1", "not a number", "2");

        // When
        Publisher<Integer> converted =
                DynamicValueConverterFactory.convertStream(input, String.class, Integer.class);

        // Then
        StepVerifier.create(converted)
                .expectNext(1)
                .expectError()
                .verify();
    }
}