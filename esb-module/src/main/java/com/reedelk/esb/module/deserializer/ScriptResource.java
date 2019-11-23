package com.reedelk.esb.module.deserializer;

public class ScriptResource {

    private final String scriptFilePath;
    private final String body;

    ScriptResource(String scriptFilePath, String body) {
        this.scriptFilePath = scriptFilePath;
        this.body = body;
    }

    public String getScriptFilePath() {
        return scriptFilePath;
    }

    public String getBody() {
        return body;
    }
}