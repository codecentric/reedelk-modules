package com.esb.component;

import com.esb.api.component.Join;
import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.esb.commons.Preconditions.checkState;

public class Fork implements FlowControlComponent {

    private List<ExecutionNode> forkNodes = new ArrayList<>();
    private ExecutionNode join;

    private CompletionService<Message> completionService;

    @Override
    public List<ExecutionNode> apply(Message input) {
        return forkNodes;
    }

    public void addJoin(ExecutionNode joinComponent) {
        checkState(joinComponent.getComponent() instanceof Join, "Join Component must implement interface 'Join'");
        this.join = joinComponent;
    }

    public void addForkNode(ExecutionNode executionNode) {
        this.forkNodes.add(executionNode);
    }

    public ExecutionNode getJoin() {
        return join;
    }


    public void setThreadPoolSize(int threadPoolSize) {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        completionService = new ExecutorCompletionService<>(executor);
    }

    public List<Message> invokeAllAndWait(List<Callable<Message>> allTasks) {
        List<Message> results = new ArrayList<>();
        try {
            allTasks.forEach(messageCallable -> completionService.submit(messageCallable));
            int received = 0;
            boolean errors = false;

            while (received < allTasks.size() && !errors) {
                Future<Message> resultFuture = completionService.take();
                try {
                    Message result = resultFuture.get();
                    received++;
                    results.add(result);

                } catch (Exception e) {
                    errors = true;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return results;
    }
}
