package com.esb.admin.console.dev.commons;

import org.takes.Request;
import org.takes.rq.RqPrint;

import java.io.IOException;
import java.util.Scanner;

public class RequestBody {

    private RequestBody() {
    }

    public static String from(Request request) throws IOException {
        String body = new RqPrint(request).printBody();
        Scanner s = new Scanner(body).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
