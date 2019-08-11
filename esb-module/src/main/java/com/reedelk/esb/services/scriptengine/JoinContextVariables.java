package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.message.Message;

import javax.script.SimpleBindings;
import java.util.List;

public class JoinContextVariables extends SimpleBindings {

    public JoinContextVariables(List<Message> messages) {
        put("messages", messages);
    }
}
