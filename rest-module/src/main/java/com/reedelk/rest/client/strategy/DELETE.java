package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.body.BodyProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class DELETE extends BaseStrategyWithBody {

    DELETE(boolean chunked) {
        super(chunked);
    }

    @Override
    protected HttpEntityEnclosingRequestBase request(BodyProvider bodyProvider) {
        return new HttpDeleteWithBody();
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
