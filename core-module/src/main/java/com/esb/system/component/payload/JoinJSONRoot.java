package com.esb.system.component.payload;

import com.esb.api.annotation.Default;
import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Required;
import com.esb.api.component.Join;
import com.esb.api.message.MemoryTypedContent;
import com.esb.api.message.Message;
import com.esb.api.message.MimeType;
import com.esb.api.message.Type;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Join JSON")
@Component(service = JoinJSONRoot.class, scope = PROTOTYPE)
public class JoinJSONRoot implements Join {

    @Property("Root property to join data on")
    @Required
    @Default("property1")
    private String rootPropertyToJoin;

    @Override
    public Message apply(List<Message> messagesToJoin) {
        List<JSONObject> objects = messagesToJoin
                .stream()
                .map(message -> {
                    String content = (String) message.getTypedContent().getContent();
                    return new JSONObject(content);
                })
                .collect(Collectors.toList());

        JSONArray merged = new JSONArray();
        objects.forEach(object -> {
            JSONArray currentArray = object.getJSONArray(rootPropertyToJoin);
            for (int i = 0; i < currentArray.length(); i++) {
                merged.put(currentArray.get(i));
            }
        });

        JSONObject result = new JSONObject();
        result.put(rootPropertyToJoin, merged);

        String finalJson = result.toString(2);

        Message message = new Message();
        Type type = new Type(MimeType.APPLICATION_JSON, String.class);
        MemoryTypedContent<String> typedContent = new MemoryTypedContent<>(finalJson, type);
        message.setTypedContent(typedContent);
        return message;
    }

    public void setRootPropertyToJoin(String rootPropertyToJoin) {
        this.rootPropertyToJoin = rootPropertyToJoin;
    }
}
