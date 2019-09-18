package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class DELETE extends BaseStrategyWithBody {

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
