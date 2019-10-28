package com.reedelk.admin.console;

import com.reedelk.runtime.api.exception.ESBException;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class ByteArrayUtils {

    static byte[] readFrom(InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = inputStream.read(buffer); len != -1; len = inputStream.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

    static void writeTo(String fileName, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(data);
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }
}
