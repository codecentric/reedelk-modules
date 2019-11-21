package com.reedelk.esb.test.utils;

import com.reedelk.esb.commons.ConfigPropertyAwareTypeFactory;
import com.reedelk.esb.flow.FlowBuilderContext;
import org.osgi.framework.Bundle;

public class MockFlowBuilderContext extends FlowBuilderContext {
    public MockFlowBuilderContext(Bundle bundle) {
        super(bundle, null, null, new ConfigPropertyAwareTypeFactory(null));
    }
}
