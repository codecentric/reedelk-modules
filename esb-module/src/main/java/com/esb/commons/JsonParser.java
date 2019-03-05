package com.esb.commons;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonParser {

    private JsonParser() {
    }

    public static JSONObject from(String json) {
        return new JSONObject(json);
    }

    public interface Implementor {

        static String name() {
            return "implementor.name";
        }

        static String name(JSONObject implementorDefinition) {
            return implementorDefinition.getString(name());
        }

        static void name(String implementorName, JSONObject implementorDefinition) {
            implementorDefinition.put(name(), implementorName);
        }

    }

    public interface Config {

        static String id() {
            return "id";
        }

        static String id(JSONObject configDefinition) {
            return configDefinition.getString(id());
        }

    }

    public interface Component {

        static String configRef() {
            return "configRef";
        }

        static String configRef(JSONObject componentDefinition) {
            return componentDefinition.getString(configRef());
        }
    }

    public static class Subflow {

        private Subflow() {
        }

        public static String id(JSONObject definition) {
            return definition.getString("id");
        }

        public static JSONArray getSubflow(JSONObject definition) {
            return definition.getJSONArray("subflow");
        }
    }

    public static class Flow {

        private Flow() {
        }

        public static String id(JSONObject definition) {
            return definition.getString("id");
        }

        public static boolean hasId(JSONObject definition) {
            return definition.has("id");
        }

        public static JSONArray getFlow(JSONObject definition) {
            return definition.getJSONArray("flow");
        }
    }


    public static class Choice implements Component {

        private Choice() {
        }

        public static JSONArray getWhen(JSONObject componentDefinition) {
            return componentDefinition.getJSONArray("when");
        }

        public static JSONArray getNext(JSONObject componentDefinition) {
            return componentDefinition.getJSONArray("next");
        }

        public static JSONArray getOtherwise(JSONObject componentDefinition) {
            return componentDefinition.getJSONArray("otherwise");
        }

        public static String getCondition(JSONObject componentDefinition) {
            return componentDefinition.getString("condition");
        }

    }

    public static class ForkJoin implements Component {

        private ForkJoin() {
        }

        public static JSONArray getFork(JSONObject componentDefinition) {
            return componentDefinition.getJSONArray("fork");
        }

        public static JSONArray getNext(JSONObject componentDefinition) {
            return componentDefinition.getJSONArray("next");
        }

        public static JSONObject getJoin(JSONObject componentDefinition) {
            return componentDefinition.getJSONObject("join");
        }

        public static int getThreadPoolSize(JSONObject componentDefinition) {
            return componentDefinition.getInt("threadPoolSize");
        }
    }

    public static class FlowReference implements Component {

        private FlowReference() {
        }

        public static String getRef(JSONObject componentDefinition) {
            return componentDefinition.getString("ref");
        }

    }
}
