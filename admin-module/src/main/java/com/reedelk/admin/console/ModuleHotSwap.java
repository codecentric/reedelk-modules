package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.rest.api.InternalAPI;
import com.reedelk.runtime.rest.api.hotswap.v1.HotSwapPOSTReq;
import com.reedelk.runtime.rest.api.hotswap.v1.HotSwapPOSTRes;
import com.reedelk.runtime.system.api.BundleNotFoundException;
import com.reedelk.runtime.system.api.HotSwapService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Module hot swap")
@Component(service = ModuleHotSwap.class, scope = PROTOTYPE)
public class ModuleHotSwap implements ProcessorSync {

    private static final int STATUS_NOT_FOUND = 404;
    private static final String VAR_ERROR_RESPONSE_CODE_NAME = "errorResponseCode";

    @Reference
    private HotSwapService hotSwapService;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String payload = message.payload();

        String resultJson = hotSwap(payload, flowContext);

        return MessageBuilder.get().json(resultJson).build();
    }

    private String hotSwap(String json, FlowContext flowContext) {
        HotSwapPOSTReq hotSwapReq = InternalAPI.HotSwap.V1.POST.Req.deserialize(json);

        long hotSwappedModuleId;
        try {
            hotSwappedModuleId = hotSwapService.hotSwap(hotSwapReq.getModuleFilePath(), hotSwapReq.getResourcesRootDirectory());
        } catch (BundleNotFoundException e) {
            // If we  tried to Hot swap a module which was
            // not installed in the runtime, we return
            // status code 'Not Found' - 404.
            flowContext.setVariable(VAR_ERROR_RESPONSE_CODE_NAME, STATUS_NOT_FOUND);
            throw new ESBException(e);
        }

        HotSwapPOSTRes dto = new HotSwapPOSTRes();
        dto.setModuleId(hotSwappedModuleId);
        return InternalAPI.HotSwap.V1.POST.Res.serialize(dto);
    }
}
