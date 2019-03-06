package com.esb.test.utils;

import java.net.URL;

public enum TestJson {

    FLOW_WITH_COMPONENTS {
        @Override
        String path() {
            return "/com/esb/lifecycle/flow_with_some_components.json";
        }
    },

    FLOW_WITH_CHOICE {
        @Override
        String path() {
            return "/com/esb/lifecycle/flow_with_choice.json";
        }
    },

    FLOW_WITHOUT_ID {
        @Override
        String path() {
            return "/com/esb/lifecycle/flow_without_id.json";
        }
    },

    FLOW_WITH_NOT_WELL_FORMED_CHOICE {
        @Override
        String path() {
            return "/com/esb/lifecycle/flow_with_not_well_formed_choice.json";
        }
    },

    SUBFLOW_WITH_COMPONENTS {
        @Override
        String path() {
            return "/com/esb/lifecycle/subflow_with_some_components.json";
        }
    },

    CONFIG {
        @Override
        String path() {
            return "/com/esb/lifecycle/config.json";
        }
    };

    public URL url() {
        return TestJson.class.getResource(path());
    }

    abstract String path();

}
