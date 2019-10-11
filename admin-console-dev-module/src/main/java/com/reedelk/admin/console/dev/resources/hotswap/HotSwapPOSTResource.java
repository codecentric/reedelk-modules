package com.reedelk.admin.console.dev.resources.hotswap;

import com.reedelk.admin.console.dev.commons.RequestBody;
import com.reedelk.runtime.rest.api.InternalAPI;
import com.reedelk.runtime.rest.api.hotswap.v1.HotSwapPOSTReq;
import com.reedelk.runtime.rest.api.hotswap.v1.HotSwapPOSTRes;
import com.reedelk.runtime.system.api.BundleNotFoundException;
import com.reedelk.runtime.system.api.HotSwapService;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithStatus;

import java.io.IOException;

import static com.reedelk.admin.console.dev.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.runtime.api.message.content.MimeType.APPLICATION_JSON;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public class HotSwapPOSTResource implements Take {

    private final HotSwapService service;

    HotSwapPOSTResource(HotSwapService service) {
        this.service = service;
    }

    @Override
    public Response act(Request request) throws IOException {
        String json = RequestBody.from(request);
        HotSwapPOSTReq hotSwapReq = InternalAPI.HotSwap.V1.POST.Req.deserialize(json);

        long hotSwappedModuleId;
        try {
            hotSwappedModuleId = service.hotSwap(hotSwapReq.getModuleFilePath(), hotSwapReq.getResourcesRootDirectory());
        } catch (BundleNotFoundException e) {
            // If we  tried to Hot swap a module which was
            // not installed in the runtime, we return
            // status code 'Not Found' - 404.
            return new RsWithStatus(HTTP_NOT_FOUND);
        }

        HotSwapPOSTRes dto = new HotSwapPOSTRes();
        dto.setModuleId(hotSwappedModuleId);
        return new RsWithBody(
                new RsWithStatus(
                        new RsWithHeader(CONTENT_TYPE, APPLICATION_JSON.toString()), HTTP_OK),
                InternalAPI.HotSwap.V1.POST.Res.serialize(dto));
    }
}
