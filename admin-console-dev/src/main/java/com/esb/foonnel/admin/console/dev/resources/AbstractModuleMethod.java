package com.esb.foonnel.admin.console.dev.resources;

import com.esb.foonnel.internal.api.module.v1.ModuleService;
import org.takes.Request;
import org.takes.Take;
import org.takes.rq.RqPrint;

import java.io.IOException;
import java.util.Scanner;

abstract class AbstractModuleMethod implements Take {

    final ModuleService service;

    AbstractModuleMethod(ModuleService service) {
        this.service = service;
    }

    static String body(Request request) throws IOException {
        String body = new RqPrint(request).printBody();
        Scanner s = new Scanner(body).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
