package com.reedelk.esb.services.scriptengine.converter.bytearraytype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import org.reactivestreams.Publisher;

public class AsByteArray implements DynamicValueConverter<byte[], byte[]> {

    @Override
    public byte[] from(byte[] value) {
        return value;
    }

    @Override
    public Publisher<byte[]> from(Publisher<byte[]> stream) {
        return stream;
    }
}
