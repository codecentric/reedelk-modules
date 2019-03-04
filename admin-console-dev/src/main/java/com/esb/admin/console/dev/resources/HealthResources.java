package com.esb.admin.console.dev.resources;

import com.esb.internal.api.InternalAPI;
import com.esb.internal.api.SystemProperty;
import com.esb.internal.api.health.v1.HealthGETRes;
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

import static com.esb.admin.console.dev.HttpMethod.GET;

public class HealthResources implements Fork {

    private static final String BASE_PATH = "/health";

    private final FkRegex fkRegex;

    public HealthResources(SystemProperty systemProperty) {
        String responseJson = buildResponse(systemProperty);
        fkRegex = new FkRegex(BASE_PATH, new TkFork(
                new FkMethods(GET.name(),
                        new RsWithBody(
                                new RsWithHeader("Content-Type: application/json"), responseJson.getBytes()))));
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
