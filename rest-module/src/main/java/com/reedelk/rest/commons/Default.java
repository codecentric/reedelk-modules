package com.reedelk.rest.commons;

public class Default {

    public static final int DEFAULT_MAX_INITIAL_LINE_LENGTH = 4096; // in bytes
    public static final int DEFAULT_MAX_LENGTH_OF_ALL_HEADERS = 8192; // in bytes
    public static final int DEFAULT_MAX_CHUNK_SIZE = 8192; // in bytes
    public static final int DEFAULT_MAX_CONTENT_SIZE = 100 * 1024 * 1024; // 100 MB
    public static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = 30 * 1000; // 30 seconds

    public static final boolean DEFAULT_VALIDATE_HEADERS = false;
}
