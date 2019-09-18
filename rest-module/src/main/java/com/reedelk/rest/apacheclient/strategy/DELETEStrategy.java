package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BasicHttpEntity;

public class DELETEStrategy extends AbstractStrategy {

    @Override
    protected HttpRequestBase baseRequest(BodyProvider bodyProvider) {
        HttpDeleteWithBody delete = new HttpDeleteWithBody();
        delete.setEntity(new BasicHttpEntity());
        return delete;
    }

    public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

        final static String METHOD_NAME = "DELETE";

        private HttpDeleteWithBody() {
            super();
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

    }
}
