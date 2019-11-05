package com.reedelk.esb.execution;

import com.reedelk.runtime.api.message.FlowContext;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFlowContext extends ConcurrentHashMap<String, Serializable> implements FlowContext {
}
