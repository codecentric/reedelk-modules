package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.JavascriptEngineProvider;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.StringContent;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.script.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DynamicValueStreamEvaluatorTest {

    @Mock
    private FlowContext context;

    private DynamicValueStreamEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new DynamicValueStreamEvaluator(JavascriptEngineProvider.INSTANCE);
    }

    @Nested
    @DisplayName("Evaluate dynamic string value with message and context")
    class EvaluateDynamicStringValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateMessageAttributeProperty() {
            // Given
            Message message = MessageBuilder.get().text("this is a test").build();
            message.getAttributes().put("property1", "test1");
            DynamicString dynamicString = DynamicString.from("#[message.attributes.property1]");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("test1")
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateTextPayload() {
            // Given
            Message message = MessageBuilder.get().text("this is a test").build();
            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("this is a test")
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateStreamPayload() {
            // Given
            TypedContent<String> typedContent = new StringContent(Flux.just("one", "two"), MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("one")
                    .expectNext("two")
                    .verifyComplete();
        }

        // Here the original stream MUST be consumed
        // in order to evaluate the script.
        @Test
        void shouldCorrectlyConcatenateStreamWithString() {
            // Given
            Flux<String> content = Flux.just("Hello", ", this", " is", " just", " a");

            TypedContent<String> typedContent = new StringContent(content, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.content.data() + ' test.']");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("Hello, this is just a test.")
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyConcatenateWithString() {
            // Given
            String payload = "Hello, this is just a";
            TypedContent<String> typedContent = new StringContent(payload, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.content.data() + ' test.']");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("Hello, this is just a test.")
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateString() {
            // Given
            Message message = MessageBuilder.get().text("test").build();

            DynamicString dynamicString = DynamicString.from("#['evaluation test']");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("evaluation test")
                    .verifyComplete();
        }

        @Test
        void shouldReturnTextFromDynamicValue() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("Expected text");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("Expected text")
                    .verifyComplete();
        }

        @Test
        void shouldReturnEmptyString() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("")
                    .verifyComplete();
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueIsNull() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = null;

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .verifyComplete();
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueScriptIsEmpty() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("#[]");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .verifyComplete();
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueStringIsNull() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from(null);

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateInteger() {
            // Given
            Message message = MessageBuilder.get().javaObject(23432).build();
            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            Publisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("23432")
                    .verifyComplete();
        }
    }
}