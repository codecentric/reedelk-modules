package com.reedelk.esb.execution;

import com.reedelk.runtime.api.message.FlowContext;

import java.io.Serializable;
import java.util.HashMap;

public class DefaultFlowContext extends HashMap<String, Serializable> implements FlowContext {
}
