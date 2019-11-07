package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.execution.DefaultFlowContext;
import com.reedelk.esb.services.scriptengine.JavascriptEngineProvider;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.Script;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ScriptEvaluatorTest {

    private FlowContext context;

    private ScriptEvaluator evaluator;

    private Message emptyMessage = MessageBuilder.get().empty().build();

    @BeforeEach
    void setUp() {
        context = DefaultFlowContext.from(emptyMessage);
        evaluator = new ScriptEvaluator(JavascriptEngineProvider.INSTANCE);
    }

    @Nested
    @DisplayName("Evaluate script with message and context")
    class EvaluateScriptWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateScriptAndReturnOptional() {
            // Given
            Script stringConcatenation = Script.from("#[return 'one' + ' ' + 'two']");

            // When
            Optional<String> actual = evaluator.evaluate(stringConcatenation, emptyMessage, context, String.class);

            // Then
            assertThat(actual).isPresent().contains("one two");
        }

        @Test
        void shouldCorrectlyReturnEmptyOptionalWhenScriptIsEmpty() {
            // Given
            Script emptyScript = Script.from("#[]");

            // When
            Optional<String> actual = evaluator.evaluate(emptyScript, emptyMessage, context, String.class);

            // Then
            assertThat(actual).isNotPresent();
        }

        @Test
        void shouldCorrectlyReturnEmptyOptionalWhenScriptIsNull() {
            // Given
            Script nullScript = null;

            // When
            Optional<String> actual = evaluator.evaluate(nullScript, emptyMessage, context, String.class);

            // Then
            assertThat(actual).isNotPresent();
        }

        @Test
        void shouldThrowExceptionWhenScriptIsNotValid() {
            // Given
            Script invalidScript = Script.from("#[return 'hello]");

            // When
            ESBException exception = Assertions.assertThrows(ESBException.class,
                    () -> evaluator.evaluate(invalidScript, emptyMessage, context, String.class));

            // Then
            assertThat(exception).isNotNull();
        }

        @Test
        void shouldCorrectlyConvertIntegerResultToString() {
            // Given
            Script intScript = Script.from("#[return 2351]");

            // When
            Optional<Integer> actual = evaluator.evaluate(intScript, emptyMessage, context, Integer.class);

            // Then
            assertThat(actual).isPresent().contains(2351);
        }

        @Test
        void shouldCorrectlyEvaluateMessagePayload() {
            // Given
            Script payloadScript = Script.from("#[return message.payload()]");
            Message message = MessageBuilder.get().text("my payload as text").build();

            // When
            Optional<String> actual = evaluator.evaluate(payloadScript, message, context, String.class);

            // Then
            assertThat(actual).isPresent().contains("my payload as text");
        }

        @Test
        void shouldCorrectlyEvaluateContextVariable() {
            // Given
            context.put("messageVar", "my sample");
            Script contextVariableScript = Script.from("#[return context.messageVar]");

            // When
            Optional<String> actual = evaluator.evaluate(contextVariableScript, emptyMessage, context, String.class);

            // Then
            assertThat(actual).isPresent().contains("my sample");
        }
    }

    @Nested
    @DisplayName("Evaluate script with messages and context")
    class EvaluateScriptWithMessagesAndContext {

        private final List<Message> messages = asList(
                MessageBuilder.get().text("one").build(),
                MessageBuilder.get().text("two").build(),
                MessageBuilder.get().text("three").build());

        @Test
        void shouldCorrectlyEvaluateScriptAndReturnOptional() {
            // Given
            String concatenateMessagesScript = "" +
                    "var result = '';" +
                    "for (i = 0; i < messages.length; i++) {" +
                    "   if (i == messages.length - 1) {" +
                    "       result += messages[i].payload();" +
                    "   } else {" +
                    "       result += messages[i].payload() + ';';" +
                    "   }" +
                    "}" +
                    "return result;";

            Script stringConcatenation = Script.from("#[" + concatenateMessagesScript + "]");

            // When
            Optional<String> actual = evaluator.evaluate(stringConcatenation, messages, context, String.class);

            // Then
            assertThat(actual).isPresent().contains("one;two;three");
        }

        @Test
        void shouldCorrectlyReturnEmptyOptionalWhenScriptIsEmpty() {
            // Given
            Script emptyScript = Script.from("#[]");

            // When
            Optional<String> actual = evaluator.evaluate(emptyScript, messages, context, String.class);

            // Then
            assertThat(actual).isNotPresent();
        }

        @Test
        void shouldCorrectlyReturnEmptyOptionalWhenScriptIsNull() {
            // Given
            Script nullScript = null;

            // When
            Optional<String> actual = evaluator.evaluate(nullScript, messages, context, String.class);

            // Then
            assertThat(actual).isNotPresent();
        }

        @Test
        void shouldThrowExceptionWhenScriptIsNotValid() {
            // Given
            Script invalidScript = Script.from("#[return 'hello]");

            // When
            ESBException exception = Assertions.assertThrows(ESBException.class,
                    () -> evaluator.evaluate(invalidScript, messages, context, String.class));

            // Then
            assertThat(exception).isNotNull();
        }
    }

    @Nested
    @DisplayName("Evaluate script stream with message and context")
    class EvaluateScriptStreamWithMessageAndContext {

        @Test
        void shouldReturnByteStreamFromString() {
            // Given
            Script textValuedScript = Script.from("#[return 'my test']");

            // When
            TypedPublisher<byte[]> actual = evaluator.evaluateStream(textValuedScript, emptyMessage, context, byte[].class);

            // Then
            assertThat(actual.getType()).isEqualTo(byte[].class);
            StepVerifier.create(actual)
                    .expectNextMatches(bytes -> Arrays.equals(bytes, "my test".getBytes()))
                    .verifyComplete();
        }

        @Test
        void shouldReturnOriginalStreamFromMessagePayload() {
            // Given
            Flux<byte[]> stream = Flux.just("one".getBytes(), "two".getBytes());
            ByteArrayContent byteArrayContent = new ByteArrayContent(stream, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(byteArrayContent).build();

            Script extractStreamScript = Script.from("#[message.payload()]");

            // When
            TypedPublisher<byte[]> actual = evaluator.evaluateStream(extractStreamScript, message, context, byte[].class);

            // Then
            assertThat(actual.getType()).isEqualTo(byte[].class);
            StepVerifier.create(actual)
                    .expectNextMatches(bytes -> Arrays.equals(bytes, "one".getBytes()))
                    .expectNextMatches(bytes -> Arrays.equals(bytes, "two".getBytes()))
                    .verifyComplete();
        }

        @Test
        void shouldReturnNullStreamWhenScriptIsNull() {
            // Given
            Script nullScript = null;

            // When
            Publisher<byte[]> actual = evaluator.evaluateStream(nullScript, emptyMessage, context, byte[].class);

            // Then
            assertThat(actual).isNull();
        }

        @Test
        void shouldReturnEmptyStreamWhenScriptIsEmpty() {
            // Given
            Script emptyScript = Script.from("#[]");

            // When
            Publisher<byte[]> actual = evaluator.evaluateStream(emptyScript, emptyMessage, context, byte[].class);

            // Then
            StepVerifier.create(actual).verifyComplete();
        }

        @Test
        void shouldReturnEmptyStreamWhenScriptReturnsNull() {
            // Given
            Script scriptReturningNull = Script.from("#[return null]");

            // When
            Publisher<byte[]> actual = evaluator.evaluateStream(scriptReturningNull, emptyMessage, context, byte[].class);

            // Then
            StepVerifier.create(actual).verifyComplete();
        }
    }
}