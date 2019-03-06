package com.esb.admin.console.dev.resources.hotswap;

import com.esb.admin.console.dev.commons.RequestBody;
import com.esb.internal.api.HotSwapService;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;

import java.io.IOException;

public class HotSwapPOSTResource implements Take {

    private final HotSwapService service;

    HotSwapPOSTResource(HotSwapService service) {
        this.service = service;
    }

    @Override
    public Response act(Request request) throws IOException {
        String json = RequestBody.from(request);
        //ModulePOSTReq postRequest = InternalAPI.Module.V1.POST.Req.deserialize(json);

        service.hotSwap("", "");
        return null;
    }
}
