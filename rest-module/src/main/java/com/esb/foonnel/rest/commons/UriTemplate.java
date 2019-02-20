package com.esb.foonnel.rest.commons;


import java.util.regex.Matcher;

public class UriTemplate {

    private final String uriTemplate;
    private final UriTemplateStructure uriTemplateStructure;

    public UriTemplate(String uriTemplate) {
        Preconditions.isNotNull(uriTemplate, "Uri Template");
        this.uriTemplate = uriTemplate;
        this.uriTemplateStructure = UriTemplateStructure.from(uriTemplate);
    }

    public boolean matches(String uri) {
        if (uri == null) return false;

        Matcher matcher = uriTemplateStructure.getPattern().matcher(uri);
        return matcher.matches();
    }

}
