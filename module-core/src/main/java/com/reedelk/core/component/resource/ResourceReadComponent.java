package com.reedelk.core.component.resource;

import com.reedelk.runtime.api.commons.FileUtils;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.message.DefaultMessageAttributes;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.content.MimeType;

import static com.reedelk.runtime.api.commons.ImmutableMap.of;

abstract class ResourceReadComponent {

    private final String attributeResourcePath =  "resourcePath";
    private final String attributeTimestamp = "timestamp";


    protected MessageAttributes createAttributes(Class<? extends Component> componentClazz, String resourceFilePath) {
        return new DefaultMessageAttributes(componentClazz,
                        of(attributeResourcePath, resourceFilePath,
                                attributeTimestamp, System.currentTimeMillis()));
    }

    protected static MimeType mimeTypeFrom(boolean autoMimeType, String mimeType, String filePath) {
        if (autoMimeType) {
            String pageFileExtension = FileUtils.getExtension(filePath);
            return MimeType.fromFileExtension(pageFileExtension);
        } else {
            return MimeType.parse(mimeType);
        }
    }
}
