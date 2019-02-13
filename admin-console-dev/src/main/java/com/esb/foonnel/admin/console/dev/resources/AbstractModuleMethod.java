package com.esb.foonnel.admin.console.dev.resources;

import com.esb.foonnel.internal.api.module.v1.ModuleService;
import org.takes.Request;
import org.takes.Take;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Scanner;

abstract class AbstractModuleMethod implements Take {

    protected final ModuleService service;

    AbstractModuleMethod(ModuleService service) {
        this.service = service;
    }

    protected static String decode(String value) {
        try {
            return URLDecoder.decode(value, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException var2) {
            throw new IllegalStateException(var2);
        }
    }

    protected static String body(Request request) throws IOException {
        InputStream body = request.body();
        Scanner s = new Scanner(body).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
