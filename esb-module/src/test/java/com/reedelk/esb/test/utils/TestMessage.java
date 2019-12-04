package com.reedelk.esb.test.utils;

import com.reedelk.runtime.commons.FileUtils;

import java.net.URL;

public enum TestMessage {

    FLOW_ERROR_MESSAGE_DEFAULT {
        @Override
        String path() {
            return "/com/reedelk/esb/commons/flow_error_default_message.json";
        }
    };

    abstract String path();

    public String get() {
        URL fileURL = TestMessage.class.getResource(path());
        return FileUtils.ReadFromURL.asString(fileURL);
    }
}
