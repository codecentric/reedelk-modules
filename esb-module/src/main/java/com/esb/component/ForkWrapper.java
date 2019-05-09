package com.esb.component;

import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ForkWrapper extends Fork implements FlowControlComponent {

    private List<ExecutionNode> forkNodes = new ArrayList<>();

    private CompletionService<Message> completionService;

    private ExecutionNode stopNode;

    @Override
    public List<ExecutionNode> apply(Message input) {
        return forkNodes;
    }

    public void addForkNode(ExecutionNode executionNode) {
        this.forkNodes.add(executionNode);
    }

    public ExecutionNode getStopNode() {
        return this.stopNode;
    }

    public void setStopNode(ExecutionNode stopNode) {
        this.stopNode = stopNode;
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
