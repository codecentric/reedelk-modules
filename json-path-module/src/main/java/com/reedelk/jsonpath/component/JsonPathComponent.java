package com.reedelk.jsonpath.component;

import com.jayway.jsonpath.JsonPath;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.StringContent;
import com.reedelk.runtime.api.message.type.TypedContent;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import static com.reedelk.runtime.api.message.type.MimeType.APPLICATION_JSON;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Json Path")
@Component(service = JsonPathComponent.class, scope = PROTOTYPE)
public class JsonPathComponent implements ProcessorSync {

    @Property("JsonPath Expression")
    private String jsonPathExpression;

    private JsonPath compiledExpression;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        if (compiledExpression == null) {
            compiledExpression = JsonPath.compile(jsonPathExpression);
        }

        TypedContent typedContent = message.getContent();

        String inputJson = (String) typedContent.data();

        Object result = JsonPath.parse(inputJson).read(compiledExpression);
        String jsonResult = "";
        if (result instanceof JSONArray) {
            JSONArray array = (JSONArray) result;
            jsonResult = array.toJSONString();
        } else if (result instanceof JSONObject) {
            JSONObject object = (JSONObject) result;
            jsonResult = object.toJSONString();
        } else if (result instanceof String) {
            jsonResult = (String) result;
        }

        TypedContent outputContent = new StringContent(jsonResult, APPLICATION_JSON);

        Message outputMessage = new Message();

        outputMessage.setContent(outputContent);

        return outputMessage;
    }

    public void setJsonPathExpression(String jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
    }
}
