package com.reedelk.esb.flow.component.builder;

import com.reedelk.esb.graph.ExecutionNode;
import org.json.JSONObject;

// TODO: This should be named deserializer,  look at the plugin!!!
public interface Builder {

    ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition);

}

