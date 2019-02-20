package com.esb.foonnel.rest.http;


public interface Handler {

    Response handle(Request request) throws Exception;

}