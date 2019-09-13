package com.reedelk.rest.client;

public interface HttpResponseAttribute {

    /**
     *   attributes=HttpResponseAttributes[
     *   	statusCode=200,
     *   	reasonPhrase=OK,
     *   	headers=MultiMap{[
     *   		date=[Fri, 13 Sep 2019 12:38:25 GMT],
     *   		content-length=[256],
     *   		server=[Cowboy],
     *   		expires=[Fri, 13 Sep 2019 14:38:25 GMT],
     *   		connection=[keep-alive],
     *   		content-type=[application/json],
     *   		via=[1.1 vegur]
     *   	]}
     *   ]
     */
    static String statusCode() {
        return "statusCode";
    }

    static String reasonPhrase() {
        return "reasonPhrase";
    }

    static String headers() {
        return "headers";
    }

}
