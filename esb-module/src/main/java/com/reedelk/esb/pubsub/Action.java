package com.reedelk.esb.pubsub;

public class Action {

    private Action() {
    }

    public static class Module {

        private Module() {
        }

        public static final String Uninstalled = "action#module#uninstall";

        public static class ActionModuleUninstalled extends Post<Long> {

            public ActionModuleUninstalled(long moduleId) {
                super(moduleId);
            }
        }
    }
}
