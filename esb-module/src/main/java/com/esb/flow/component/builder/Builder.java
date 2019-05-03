package com.esb.flow.component.builder;

import com.esb.flow.ExecutionNode;
import org.json.JSONObject;

// TODO: This should be named deserializer,  look at the plugin!!!
public interface Builder {

    ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition);

}

