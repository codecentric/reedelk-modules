package com.reedelk.file.commons;

public class Defaults {

    public class FileRead {

        private FileRead() {
        }

        public static final int READ_FILE_BUFFER_SIZE = 65536;
        public static final int RETRY_MAX_ATTEMPTS = 3;
        public static final long RETRY_WAIT_TIME = 500;
    }

    public class FileWrite {

        private FileWrite() {
        }

        public static final int WRITE_FILE_BUFFER_SIZE = 65536;
        public static final int RETRY_MAX_ATTEMPTS = 3;
        public static final long RETRY_WAIT_TIME = 500;
    }
}
