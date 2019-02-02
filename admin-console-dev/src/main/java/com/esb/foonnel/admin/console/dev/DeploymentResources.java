package com.esb.foonnel.admin.console.dev;

import com.esb.foonnel.domain.DeploymentService;
import com.esb.foonnel.domain.DeploymentStatus;
import com.esb.foonnel.domain.Project;
import org.json.JSONArray;
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
import java.util.Set;

import static com.esb.foonnel.admin.console.dev.GenericHandler.handlerFor;
import static com.esb.foonnel.admin.console.dev.HttpMethod.*;

public class DeploymentResources implements Fork {

    private static final String BASE_PATH = "/extension";

    private final FkRegex fkRegex;

    public DeploymentResources(DeploymentService service) {
        fkRegex = new FkRegex(BASE_PATH + "/.*", new TkFork(
                new FkMethods(PUT.name(), handlerFor(service::update)),
                new FkMethods(POST.name(), handlerFor(service::installOrUpdate)),
                new FkMethods(DELETE.name(), handlerFor(service::uninstall)),
                new FkMethods(GET.name(), request -> new RsWithBody(new RsWithHeader("Content-Type: application/json"), servicestatus(service)))
        ));
    }

    private byte[] servicestatus(DeploymentService service) {
        DeploymentStatus status = service.status();
        Set<Project> projects = status.getStatus();

        JSONArray projectsStatus = new JSONArray();
        projects.forEach(project -> projectsStatus.put(create(project)));

        JSONObject root = new JSONObject();
        root.put("status", projectsStatus);

        return root.toString(4).getBytes();
    }

    private JSONObject create(Project project) {
        JSONObject projectStatus = new JSONObject();
        projectStatus.put("name", project.getName());
        projectStatus.put("state", project.getState());
        if (project.getResolvedComponents() != null) {
            projectStatus.put("resolvedComponents", project.getResolvedComponents());
        }
        if (project.getUnresolvedComponents() != null) {
            projectStatus.put("unresolvedComponents", project.getUnresolvedComponents());
        }
        if (project.getErrorMessage() != null) {
            projectStatus.put("error", project.getErrorMessage());
        }
        return projectStatus;

    }
    @Override
    public Opt<Response> route(Request request) throws IOException {
        return fkRegex.route(request);
    }
}
