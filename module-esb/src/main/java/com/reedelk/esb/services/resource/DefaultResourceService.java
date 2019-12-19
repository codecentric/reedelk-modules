package com.reedelk.esb.services.resource;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import com.reedelk.runtime.api.resource.ResourceFile;
import com.reedelk.runtime.api.resource.ResourceService;
import com.reedelk.runtime.api.script.ScriptEngineService;

public class DefaultResourceService implements ResourceService {

    private ScriptEngineService scriptEngineService;

    public DefaultResourceService(ScriptEngineService scriptEngineService) {
        this.scriptEngineService = scriptEngineService;
    }

    // TODO: Exception handling
    // TODO: Should we stream data ?? Use StreamFrom class!!!
    @Override
    public ResourceFile findResourceBy(ResourceDynamic resourceDynamic, FlowContext flowContext, Message message) {
        return scriptEngineService
                .evaluate(resourceDynamic, flowContext, message)
                .map(evaluatedPath -> {
                    byte[] data = resourceDynamic.load(evaluatedPath);
                    return new DefaultResourceFile(data, evaluatedPath);
                })
                .orElseThrow(() -> new ESBException("Script could not be evaluated"));
    }

    class DefaultResourceFile implements ResourceFile {

        private final byte[] data;
        private final String path;

        private DefaultResourceFile(byte[] data, String path) {
            this.data = data;
            this.path = path;
        }

        @Override
        public byte[] data() {
            return data;
        }

        @Override
        public String path() {
            return path;
        }
    }
}