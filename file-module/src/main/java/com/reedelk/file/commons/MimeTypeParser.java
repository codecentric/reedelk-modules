package com.reedelk.file.commons;

import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.commons.FileUtils;

public class MimeTypeParser {

    public static MimeType from(boolean autoMimeType, String mimeType, String filePath) {
        if (autoMimeType) {
            String pageFileExtension = FileUtils.getExtension(filePath);
            return MimeType.fromFileExtension(pageFileExtension);
        } else {
            return MimeType.parse(mimeType);
        }
    }
}
