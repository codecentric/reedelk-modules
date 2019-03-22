package com.esb.component;

import com.esb.api.component.Component;
import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;

import java.util.List;

public interface FlowControlComponent extends Component {

    /**
     * The method apply for a FlowControlComponent component
     * returns the child nodes to be executed after this call.
     */
    List<ExecutionNode> apply(Message input);


}
