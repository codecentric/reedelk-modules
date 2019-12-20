package com.reedelk.esb.services.resource;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import com.reedelk.runtime.api.resource.ResourceFile;
import com.reedelk.runtime.api.resource.ResourceNotFound;
import com.reedelk.runtime.api.resource.ResourceService;
import com.reedelk.runtime.api.script.ScriptEngineService;
import org.reactivestreams.Publisher;

public class DefaultResourceService implements ResourceService {

    private ScriptEngineService scriptEngineService;

    public DefaultResourceService(ScriptEngineService scriptEngineService) {
        this.scriptEngineService = scriptEngineService;
    }

    @Override
    public ResourceFile find(ResourceDynamic resource, FlowContext flowContext, Message message) throws ResourceNotFound {
        return scriptEngineService
                .evaluate(resource, flowContext, message)
                .map(evaluatedPath -> {
                    Publisher<byte[]> data = resource.data(evaluatedPath);
                    return new DefaultResourceFile(data, evaluatedPath);
                })
                .orElseThrow(() -> new ResourceNotFound(resource));
    }
}