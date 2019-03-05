package com.esb.lifecycle;

import com.esb.commons.FileUtils;
import com.esb.commons.JsonParser;
import com.esb.test.utils.TestFlow;
import org.json.JSONObject;

import java.net.URL;

abstract class AbstractLifecycleTest {

    JSONObject parseFlow(TestFlow testFlow) {
        URL url = testFlow.url();
        String flowAsJson = FileUtils.readFrom(url);
        return JsonParser.from(flowAsJson);
    }

}
