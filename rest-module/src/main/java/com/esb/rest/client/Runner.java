package com.esb.rest.client;

import com.esb.rest.commons.RestMethod;

public class Runner {

    public static void main(String[] args) {
        String host = "http://www.mocky.io/v2/5cf6456032000015298cd152";
        Client client = new Client(host, RestMethod.GET);
        String make = client.make();
        System.out.println("Result: " + make);

    }
}
