package com.reedelk.rest.utils;

import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.util.Date;

public class SelfSignedCertificateBuilder {

    /** Current time minus 1 year, just in case software clock goes back due to time synchronization */
    private static final Date DEFAULT_NOT_BEFORE = new Date(System.currentTimeMillis() - 86400000L * 365);

    /** The maximum possible value in X.509 specification: 9999-12-31 23:59:59 */
    private static final Date DEFAULT_NOT_AFTER = new Date(253402300799000L);

    private static final String FULLY_QUALIFIED_DOMAIN_NAME = "localhost"; // must match the requests host.domain part.
    private static final String TARGET_FOLDER =  "/Users/lorenzo/Desktop/certsss";

    public static void main(String[] args) throws CertificateException, IOException {
        build();
    }

    public static void build() throws CertificateException, IOException {
        SelfSignedCertificate certificate = new SelfSignedCertificate(FULLY_QUALIFIED_DOMAIN_NAME, DEFAULT_NOT_BEFORE, DEFAULT_NOT_AFTER);
        File certificateFile = certificate.certificate();
        File privateKeyFile = certificate.privateKey();

        copyFile(certificateFile, new File(Paths.get(TARGET_FOLDER,"certificate.crt").toString()));
        copyFile(privateKeyFile, new File(Paths.get(TARGET_FOLDER,"private.key").toString()));
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }
        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }
}
