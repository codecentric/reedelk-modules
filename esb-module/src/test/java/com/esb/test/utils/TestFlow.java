package com.esb.test.utils;

import java.net.URL;

public enum TestFlow {

    WITH_SOME_COMPONENTS {
        @Override
        String path() {
            return "/com/esb/lifecycle/flow_with_some_components.json";
        }
    },

    WITH_CHOICE {
        @Override
        String path() {
            return "/com/esb/lifecycle/flow_with_choice.json";
        }
    },

    WITHOUT_ID {
        @Override
        String path() {
            return "/com/esb/lifecycle/flow_without_id.json";
        }
    },

    WITH_NOT_WELL_FORMED_CHOICE {
        @Override
        String path() {
            return "/com/esb/lifecycle/flow_with_not_well_formed_choice.json";
        }
    };

    public URL url() {
        return TestFlow.class.getResource(path());
    }

    abstract String path();

}
