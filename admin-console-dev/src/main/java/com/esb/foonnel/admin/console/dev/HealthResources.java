package com.esb.foonnel.admin.console.dev;

import com.esb.foonnel.internal.api.SystemProperty;
import org.json.JSONObject;
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

import static com.esb.foonnel.admin.console.dev.HttpMethod.GET;

public class HealthResources implements Fork {

    private static final String BASE_PATH = "/health";

    private final FkRegex fkRegex;

    HealthResources(SystemProperty systemProperty) {
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
        // TODO: Wrap json object and add propertiesUse an object here
        JSONObject object = new JSONObject();
        object.put("version", systemProperty.getFoonnelVersion());
        object.put("status", "UP");
        return object.toString(4);
    }
}
