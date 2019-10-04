package com.reedelk.esb.services.scriptengine.converter.bytearraytype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;

public class AsByteArray implements ValueConverter<byte[], byte[]> {

    @Override
    public byte[] from(byte[] value) {
        return value;
    }

    @Override
    public TypedPublisher<byte[]> from(TypedPublisher<byte[]> stream) {
        return stream;
    }
}
