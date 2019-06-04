package com.esb.mysql.component;

import com.esb.api.annotation.Default;
import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Required;
import com.esb.api.component.Processor;
import com.esb.api.message.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import java.util.List;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Results to JSON")
@Component(service = ResultSetToJsonComponent.class, scope = PROTOTYPE)
public class ResultSetToJsonComponent implements Processor {

    @Property("Wrapper object name")
    @Required
    @Default("results")
    private String wrapperObjectRootName;

    @Override
    public Message apply(Message input) {
        TypedContent typedContent = input.getTypedContent();
        InternalResultSet resultSet = (InternalResultSet) typedContent.getContent();
        List<List<Object>> data = resultSet.getData();
        List<String> columnNames = resultSet.getColumnNames();

        JSONArray records = new JSONArray();

        for (List<Object> record : data) {
            JSONObject recordAsJson = new JSONObject();
            for (int i = 0; i < columnNames.size(); i++) {
                recordAsJson.put(columnNames.get(i), record.get(i));
            }
            records.put(recordAsJson);
        }

        JSONObject outerObject = new JSONObject();
        outerObject.put(wrapperObjectRootName, records);

        String json = outerObject.toString(2);

        Message output = new Message();
        TypedContent<String> content = new MemoryTypedContent<>(json, new Type(MimeType.APPLICATION_JSON, String.class));
        output.setTypedContent(content);
        return output;
    }

    public void setWrapperObjectRootName(String wrapperObjectRootName) {
        this.wrapperObjectRootName = wrapperObjectRootName;
    }
}
