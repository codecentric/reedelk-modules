package com.reedelk.esb.services.scriptengine.evaluator;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Optional;

@SuppressWarnings("unchecked")
public class ValueProviders {

    public static final ValueProvider OPTIONAL_PROVIDER = new OptionalValueProvider();

    public static final ValueProvider STREAM_PROVIDER = new StreamValueProvider();

    private static class OptionalValueProvider implements ValueProvider {
        @Override
        public Optional<?> empty() {
            return Optional.empty();
        }

        @Override
        public Optional<?> from(Object value) {
            return Optional.ofNullable(value);
        }
    }

    private static class StreamValueProvider implements ValueProvider {
        @Override
        public Publisher<?> empty() {
            return Mono.empty();
        }

        @Override
        public Publisher<?> from(Object value) {
            if (value == null) {
                return Mono.empty();
            } else if (value instanceof Publisher<?>) {
                return (Publisher<?>) value;
            } else {
                return Mono.just(value);
            }
        }
    }
}
