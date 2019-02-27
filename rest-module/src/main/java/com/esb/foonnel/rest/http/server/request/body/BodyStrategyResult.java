package com.esb.foonnel.rest.http.server.request.body;

import com.esb.foonnel.api.message.Part;
import com.esb.foonnel.api.message.TypedContent;

import java.util.ArrayList;
import java.util.List;

public class BodyStrategyResult<T> {

    private final TypedContent<T> content;
    private final List<Part> parts = new ArrayList<>();

    public BodyStrategyResult(TypedContent<T> content, List<Part> parts) {
        this.content = content;
        this.parts.addAll(parts);
    }

    public TypedContent<T> getContent() {
        return content;
    }

    public List<Part> getParts() {
        return parts;
    }
}
