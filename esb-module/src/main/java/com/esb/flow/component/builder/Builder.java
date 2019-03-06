package com.esb.flow.component.builder;

import com.esb.flow.ExecutionNode;
import org.json.JSONObject;

public interface Builder {

    ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition);

}

