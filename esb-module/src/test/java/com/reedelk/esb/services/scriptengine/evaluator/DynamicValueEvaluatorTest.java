package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.JavascriptEngineProvider;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.StringContent;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.script.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DynamicValueEvaluatorTest {

    @Mock
    private FlowContext context;

    private DynamicValueEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new DynamicValueEvaluator(JavascriptEngineProvider.INSTANCE);
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
            Optional<String> evaluated = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(evaluated).isPresent().contains("test1");
        }

        @Test
        void shouldCorrectlyEvaluateTextPayload() {
            // Given
            Message message = MessageBuilder.get().text("this is a test").build();
            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            Optional<String> evaluated = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(evaluated).isPresent().contains("this is a test");
        }

        @Test
        void shouldCorrectlyEvaluateStreamPayload() {
            // Given
            TypedContent<String> typedContent = new StringContent(Flux.just("one", "two"), MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            Optional<String> evaluated = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(evaluated).isPresent().contains("onetwo");
        }

        @Test
        void shouldCorrectlyConcatenateStreamWithString() {
            // Given
            Flux<String> content = Flux.just("Hello", ", this", " is", " just", " a");

            TypedContent<String> typedContent = new StringContent(content, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.content.data() + ' test.']");

            // When
            Optional<String> result = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("Hello, this is just a test.");
        }

        @Test
        void shouldCorrectlyConcatenateWithString() {
            // Given
            String payload = "Hello, this is just a";
            TypedContent<String> typedContent = new StringContent(payload, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.content.data() + ' test.']");

            // When
            Optional<String> result = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("Hello, this is just a test.");
        }

        @Test
        void shouldCorrectlyEvaluateString() {
            // Given
            Message message = MessageBuilder.get().text("test").build();

            DynamicString dynamicString = DynamicString.from("#['evaluation test']");

            // When
            Optional<String> result = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("evaluation test");
        }

        @Test
        void shouldReturnTextFromDynamicValue() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("Expected text");

            // When
            Optional<String> result = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("Expected text");
        }

        @Test
        void shouldReturnEmptyString() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("");

            // When
            Optional<String> result = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("");
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueIsNull() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = null;

            // When
            Optional<String> result = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isNotPresent();
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueScriptIsEmpty() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("#[]");

            // When
            Optional<String> result = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isNotPresent();
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueStringIsNull() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from(null);

            // When
            Optional<String> result = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isNotPresent();
        }

        @Test
        void shouldCorrectlyEvaluateInteger() {
            // Given
            Message message = MessageBuilder.get().javaObject(23432).build();
            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            Optional<String> result = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).contains("23432");
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic integer value with message and context")
    class EvaluateDynamicIntegerValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateInteger() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicInteger dynamicInteger = DynamicInteger.from("#[506]");

            // When
            Optional<Integer> evaluated = evaluator.evaluate(dynamicInteger, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(506);
        }

        // Testing optimistic typing (Nashorn uses optimistic typing (since JDK 8u40))
        // http://openjdk.java.net/jeps/196.
        @Test
        void shouldCorrectlySumNumber() {
            // Given
            Message message = MessageBuilder.get().text("12").build();
            DynamicInteger dynamicInteger = DynamicInteger.from("#[parseInt(message.payload()) + 10]");

            // When
            Optional<Integer> evaluated = evaluator.evaluate(dynamicInteger, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(22);
        }

        @Test
        void shouldCorrectlyEvaluateIntegerFromText() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicInteger dynamicInteger = DynamicInteger.from("53");

            // When
            Optional<Integer> evaluated = evaluator.evaluate(dynamicInteger, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(53);
        }

        @Test
        void shouldCorrectlyEvaluateIntegerFromMessagePayload() {
            // Given
            Message message = MessageBuilder.get().javaObject(120).build();
            DynamicInteger dynamicInteger = DynamicInteger.from("#[message.payload()]");

            // When
            Optional<Integer> evaluated = evaluator.evaluate(dynamicInteger, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(120);
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic boolean value with message and context")
    class EvaluateDynamicBooleanValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateBoolean() {
            // Given
            Message message = MessageBuilder.get().text("a test").build();
            DynamicBoolean dynamicBoolean = DynamicBoolean.from("#[1 == 1]");

            // When
            Optional<Boolean> evaluated = evaluator.evaluate(dynamicBoolean, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(true);
        }

        @Test
        void shouldCorrectlyEvaluateBooleanFromPayload() {
            // Given
            Message message = MessageBuilder.get().text("true").build();
            DynamicBoolean dynamicBoolean = DynamicBoolean.from("#[message.payload()]");

            // When
            Optional<Boolean> evaluated = evaluator.evaluate(dynamicBoolean, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(true);
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic byte array value with message and context")
    class EvaluateDynamicByteArrayWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateByteArrayFromPayload() {
            // Given
            String payload = "My sample payload";
            Message message = MessageBuilder.get().text(payload).build();
            DynamicByteArray dynamicByteArray = DynamicByteArray.from("#[message.payload()]");

            // When
            Optional<byte[]> evaluated = evaluator.evaluate(dynamicByteArray, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(payload.getBytes());
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic object value with message and context")
    class EvaluateDynamicObjectValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateDynamicObject() {
            // Given
            Flux<String> content = Flux.just("Hello", ", this", " is", " just", " a");
            Type type = new Type(MimeType.TEXT, String.class);
            TypedContent<String> typedContent = new StringContent(content, type);

            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicObject dynamicObject = DynamicObject.from("#[message.content]");

            // When
            Optional<Object> result = evaluator.evaluate(dynamicObject, message, context);

            // Then
            assertThat(result).isPresent().containsSame(typedContent);
        }

        @Test
        void shouldCorrectlyEvaluateMessage() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicObject dynamicString = DynamicObject.from("#[message]");

            // When
            Optional<Object> evaluated = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(message);
        }

        @Test
        void shouldCorrectlyEvaluateMessagePayload() {
            // Given
            MyObject given = new MyObject();
            Message message = MessageBuilder.get().javaObject(given).build();
            DynamicObject dynamicString = DynamicObject.from("#[message.payload()]");

            // When
            Optional<Object> evaluated = evaluator.evaluate(dynamicString, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(given);
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic string with throwable and context")
    class EvaluateDynamicStringWithThrowableAndContext {

        @Test
        void shouldCorrectlyEvaluateErrorPayload() {
            // Given
            Throwable myException = new ESBException("Test error");
            DynamicString dynamicString = DynamicString.from("#[error]");

            // When
            Optional<String> evaluated = evaluator.evaluate(dynamicString, myException, context);

            // Then
            assertThat(evaluated).isPresent().contains(StackTraceUtils.asString(myException));
        }

        @Test
        void shouldCorrectlyEvaluateExceptionMessage() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = DynamicString.from("#[error.getMessage()]");

            // When
            Optional<String> evaluated = evaluator.evaluate(dynamicString, myException, context);

            // Then
            assertThat(evaluated).isPresent().contains("My exception message");
        }

        @Test
        void shouldReturnEmptyWhenScriptIsEmpty() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = DynamicString.from("#[]");

            // When
            Optional<String> evaluated = evaluator.evaluate(dynamicString, myException, context);

            // Then
            assertThat(evaluated).isNotPresent();
        }

        @Test
        void shouldReturnEmptyWhenNullString() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = DynamicString.from(null);

            // When
            Optional<String> evaluated = evaluator.evaluate(dynamicString, myException, context);

            // Then
            assertThat(evaluated).isNotPresent();
        }

        @Test
        void shouldReturnStringValue() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = DynamicString.from("my text");

            // When
            Optional<String> evaluated = evaluator.evaluate(dynamicString, myException, context);

            // Then
            assertThat(evaluated).contains("my text");
        }

        @Test
        void shouldReturnEmptyWhenNullDynamicValue() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = null;

            // When
            Optional<String> evaluated = evaluator.evaluate(dynamicString, myException, context);

            // Then
            assertThat(evaluated).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic object value with throwable and context")
    class EvaluateDynamicObjectValueWithThrowableAndContext {

        @Test
        void shouldCorrectlyEvaluateDynamicObject() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicObject dynamicObject = DynamicObject.from("#[error]");

            // When
            Optional<Object> result = evaluator.evaluate(dynamicObject, myException, context);

            // Then
            assertThat(result).isPresent().containsSame(myException);
        }

        @Test
        void shouldReturnStringDynamicObject() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicObject dynamicObject = DynamicObject.from("my text");

            // When
            Optional<Object> result = evaluator.evaluate(dynamicObject, myException, context);

            // Then
            assertThat(result).isPresent().contains("my text");
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic byte array with throwable and context")
    class EvaluateDynamicByteArrayWithThrowableAndContext {

        @Test
        void shouldCorrectlyEvaluateDynamicByteArrayFromException() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicByteArray dynamicByteArray = DynamicByteArray.from("#[error]");

            // When
            Optional<byte[]> result = evaluator.evaluate(dynamicByteArray, myException, context);

            // Then
            assertThat(result).isPresent().contains(StackTraceUtils.asByteArray(myException));
        }
    }

    private class MyObject {
    }
}