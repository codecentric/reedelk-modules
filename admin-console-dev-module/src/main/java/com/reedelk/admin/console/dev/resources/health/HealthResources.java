package com.reedelk.admin.console.dev.resources.health;

import com.reedelk.runtime.rest.api.InternalAPI;
import com.reedelk.runtime.rest.api.health.v1.HealthGETRes;
import com.reedelk.runtime.system.api.SystemProperty;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.misc.Opt;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;

import java.io.IOException;

import static com.reedelk.admin.console.dev.HttpMethod.GET;
import static com.reedelk.admin.console.dev.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.runtime.api.message.content.MimeType.APPLICATION_JSON;

public class HealthResources implements Fork {

    private final FkRegex fkRegex;

    public HealthResources(SystemProperty systemProperty, String path) {
        String responseJson = buildResponse(systemProperty);
        fkRegex = new FkRegex(path, new TkFork(
                new FkMethods(GET.name(),
                        new RsWithBody(
                                new RsWithHeader(CONTENT_TYPE, APPLICATION_JSON.toString()), responseJson.getBytes()))));
    }

    @Override
    public Opt<Response> route(Request request) throws IOException {
        return fkRegex.route(request);
    }

    private String buildResponse(SystemProperty systemProperty) {
        HealthGETRes health = new HealthGETRes();
        health.setStatus("UP");
        health.setVersion(systemProperty.version());
        return InternalAPI.Health.V1.GET.Res.serialize(health);
    }
}
