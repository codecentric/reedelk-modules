package com.reedelk.esb.test.utils;

import com.reedelk.esb.commons.ConfigPropertyAwareTypeFactory;
import com.reedelk.esb.flow.FlowBuilderContext;

public class MockFlowBuilderContext extends FlowBuilderContext {
    public MockFlowBuilderContext() {
        super(null, null, null, new ConfigPropertyAwareTypeFactory(null));
    }
}
