package com.esb.foonnel.rest;

import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RESTRequestor.class, scope = PROTOTYPE)
public class RESTRequestor {
}
