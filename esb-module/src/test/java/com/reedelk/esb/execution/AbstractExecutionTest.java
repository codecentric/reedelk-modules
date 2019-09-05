package com.reedelk.esb.execution;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.test.utils.TestInboundComponent;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.component.Stop;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractExecutionTest {

    ExecutionNode inbound = newExecutionNode(new TestInboundComponent());
    ExecutionNode stop = newExecutionNode(new Stop());

    static ExecutionNode newExecutionNode(Component component) {
        return new ExecutionNode(new ExecutionNode.ReferencePair<>(component));
    }

    MessageAndContext newEventWithContent(String content) {
        Message message = MessageBuilder.get().text(content).build();
        return new NoActionResultMessageAndContext(message);
    }

    ExecutionGraph newGraphSequence(ExecutionNode... executionNodes) {
        ExecutionGraph graph = ExecutionGraph.build();
        ExecutionNode previous = null;
        for (ExecutionNode executionNode : executionNodes) {
            if (previous == null) {
                graph.putEdge(null, executionNode);
            } else {
                graph.putEdge(previous, executionNode);
            }
            previous = executionNode;
        }
        return graph;
    }

    Consumer<MessageAndContext> assertMessageContains(String expected) {
        return event -> {
            String out = event.getMessage().getTypedContent().asString();
            assertThat(out).isEqualTo(expected);
        };
    }

    Consumer<MessageAndContext> assertMessageContainsOneOf(String... expected) {
        return event -> {
            String out = event.getMessage().getTypedContent().asString();
            assertThat(expected).contains(out);
        };
    }

    class NoActionResultMessageAndContext extends MessageAndContext {
        NoActionResultMessageAndContext(Message message) {
            super(message, new DefaultContext());
        }
    }

    class ProcessorThrowingExceptionSync implements ProcessorSync {
        @Override
        public Message apply(Message input, Context context) {
            throw new IllegalStateException("Input not valid");
        }
    }

    class AddPostfixSyncProcessor implements ProcessorSync {

        private final String postfix;

        AddPostfixSyncProcessor(String postfix) {
            this.postfix = postfix;
        }

        @Override
        public Message apply(Message input, Context context) {
            String inputString = input.getTypedContent().asString();
            String outputString = inputString + postfix;
            return MessageBuilder.get().text(outputString).build();
        }
    }
}
