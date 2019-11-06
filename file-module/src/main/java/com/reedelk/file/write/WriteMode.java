package com.reedelk.file.write;

import com.reedelk.runtime.api.annotation.DisplayName;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

public enum WriteMode {

    @DisplayName("Overwrite")
    OVERWRITE {
        @Override
        public OpenOption[] options() {
            return new OpenOption[]{
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING};
        }
    },

    @DisplayName("Create new")
    CREATE_NEW {
        @Override
        public OpenOption[] options() {
            return new OpenOption[]{
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE_NEW};
        }
    },

    @DisplayName("Append")
    APPEND {
        @Override
        public OpenOption[] options() {
            return new OpenOption[]{
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND};
        }
    };

    abstract OpenOption[] options();

}
