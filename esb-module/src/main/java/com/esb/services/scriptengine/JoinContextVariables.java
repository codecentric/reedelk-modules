package com.esb.services.scriptengine;

import com.esb.api.message.Message;

import javax.script.SimpleBindings;
import java.util.List;

public class JoinContextVariables extends SimpleBindings {

    public JoinContextVariables(List<Message> messages) {
        put("messages", messages);
    }
}
