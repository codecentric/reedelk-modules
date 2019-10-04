    package com.reedelk.esb.services.scriptengine.converter;

    import com.reedelk.runtime.api.message.type.TypedPublisher;
    import org.junit.jupiter.api.Test;
    import org.reactivestreams.Publisher;
    import reactor.core.publisher.Flux;
    import reactor.test.StepVerifier;

class ValueConverterFactoryTest {

    @Test
    void shouldConvertStringStreamToIntStream() {
        // Given
        TypedPublisher<String> input = TypedPublisher.fromString(Flux.just("1","2","4"));

        // When
        Publisher<Integer> converted =
                ValueConverterFactory.convertTypedPublisher(input, String.class, Integer.class);

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
                ValueConverterFactory.convertTypedPublisher(input, String.class, Integer.class);

        // Then
        StepVerifier.create(converted)
                .expectNext(1)
                .expectError()
                .verify();
    }
}