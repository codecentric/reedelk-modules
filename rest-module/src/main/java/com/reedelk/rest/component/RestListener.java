package com.reedelk.rest.component;

import com.reedelk.rest.configuration.RestListenerConfiguration;
import com.reedelk.rest.configuration.RestListenerErrorResponse;
import com.reedelk.rest.configuration.RestListenerResponse;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.rest.server.Server;
import com.reedelk.rest.server.ServerProvider;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.AbstractInbound;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("REST Listener")
@Component(service = RestListener.class, scope = PROTOTYPE)
public class RestListener extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(RestListener.class);

    @Reference
    private ServerProvider provider;

    @Property("Path")
    @Default("/")
    @Hint("/resource")
    private String path;

    @Property("Method")
    @Default("GET")
    private RestMethod method;

    @Property("Listener Configuration")
    private RestListenerConfiguration configuration;

    @Property("Response")
    private RestListenerResponse response;

    @Property("Error response")
    private RestListenerErrorResponse errorResponse;

    @Override
    public void onStart() {
        requireNonNull(configuration, "configuration");
        // TODO: What would happen if we cannot start the server?
        Server server = provider.get(configuration);
        server.addRoute(method, path,
                (request, callback) -> RestListener.this.onEvent(request, callback));
    }

    @Override
    public void onShutdown() {
        Server server = provider.get(configuration);
        server.removeRoute(method, path);
        try {
            provider.release(server);
        } catch (Exception e) {
            logger.error("Shutdown RESTListener", e);
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }

    public void setConfiguration(RestListenerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setResponse(RestListenerResponse response) {
        this.response = response;
    }

    public void setErrorResponse(RestListenerErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
}
