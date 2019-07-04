package com.esb.rest.server.route;

import com.esb.api.exception.ESBException;
import com.esb.rest.commons.RestMethod;

public class RouteAlreadyDefinedException extends ESBException {

    public RouteAlreadyDefinedException(RestMethod method, String path) {
        super(String.format("Route for method [%s] and path [%s] is already defined", method, path));
    }
}
