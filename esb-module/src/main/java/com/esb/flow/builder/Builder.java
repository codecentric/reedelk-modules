package com.esb.flow.builder;

import com.esb.flow.ExecutionNode;
import org.json.JSONObject;

public interface Builder {

    ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition);

}

