package com.reedelk.esb.execution;

import com.reedelk.esb.commons.ComponentDisposer;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.test.utils.TestInboundComponent;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.component.Stop;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AbstractExecutionTest {

    @Mock
    protected ComponentDisposer disposer;

    ExecutionNode inbound = newExecutionNode(new TestInboundComponent());
    ExecutionNode stop = newExecutionNode(new Stop());

    static ExecutionNode newExecutionNode(ComponentDisposer disposer, Component component) {
        return new ExecutionNode(disposer, new ExecutionNode.ReferencePair<>(component));
    }

    ExecutionNode newExecutionNode(Component component) {
        return new ExecutionNode(disposer, new ExecutionNode.ReferencePair<>(component));
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
            String out = event.getMessage().getContent().asString();
            assertThat(out).isEqualTo(expected);
        };
    }

    Consumer<MessageAndContext> assertMessageContainsOneOf(String... expected) {
        return event -> {
            String out = event.getMessage().getContent().asString();
            assertThat(expected).contains(out);
        };
    }

    class NoActionResultMessageAndContext extends MessageAndContext {
        NoActionResultMessageAndContext(Message message) {
            super(message, new DefaultFlowContext());
        }
    }

    class ProcessorThrowingExceptionSync implements ProcessorSync {
        @Override
        public Message apply(Message message, FlowContext flowContext) {
            throw new IllegalStateException("Input not valid");
        }
    }

    class AddPostfixSyncProcessor implements ProcessorSync {

        private final String postfix;

        AddPostfixSyncProcessor(String postfix) {
            this.postfix = postfix;
        }

        @Override
        public Message apply(Message message, FlowContext flowContext) {
            String inputString = message.getContent().asString();
            String outputString = inputString + postfix;
            return MessageBuilder.get().text(outputString).build();
        }
    }
}
