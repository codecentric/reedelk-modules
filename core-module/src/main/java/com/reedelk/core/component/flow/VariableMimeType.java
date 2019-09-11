package com.reedelk.core.component.flow;

import com.reedelk.runtime.api.annotation.DisplayName;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.MimeType.Literal;


public enum VariableMimeType {

    @DisplayName("-- Custom --")
    NONE {
        @Override
        MimeType mapped() {
            throw new UnsupportedOperationException();
        }
    },
    @DisplayName(Literal.ANY)
    ANY {
        @Override
        MimeType mapped() {
            return MimeType.ANY;
        }
    },
    @DisplayName(Literal.XML)
    XML {
        @Override
        MimeType mapped() {
            return MimeType.XML;
        }
    },
    @DisplayName(Literal.CSS)
    CSS {
        @Override
        MimeType mapped() {
            return MimeType.CSS;
        }
    },
    @DisplayName(Literal.JSON)
    JSON {
        @Override
        MimeType mapped() {
            return MimeType.JSON;
        }
    },
    @DisplayName(Literal.HTML)
    HTML {
        @Override
        MimeType mapped() {
            return MimeType.HTML;
        }
    },
    @DisplayName(Literal.TEXT)
    TEXT {
        @Override
        MimeType mapped() {
            return MimeType.TEXT;
        }
    },
    @DisplayName(Literal.RSS)
    RSS {
        @Override
        MimeType mapped() {
            return MimeType.RSS;
        }
    },
    @DisplayName(Literal.UNKNOWN)
    UNKNOWN {
        @Override
        MimeType mapped() {
            return MimeType.UNKNOWN;
        }
    },
    @DisplayName(Literal.ATOM)
    ATOM {
        @Override
        MimeType mapped() {
            return MimeType.ATOM;
        }
    },
    @DisplayName(Literal.JAVASCRIPT)
    JAVASCRIPT {
        @Override
        MimeType mapped() {
            return MimeType.JAVASCRIPT;
        }
    },
    @DisplayName(Literal.APPLICATION_XML)
    APPLICATION_XML {
        @Override
        MimeType mapped() {
            return MimeType.APPLICATION_XML;
        }
    },
    @DisplayName(Literal.MULTIPART_MIXED)
    MULTIPART_MIXED {
        @Override
        MimeType mapped() {
            return MimeType.MULTIPART_MIXED;
        }
    },
    @DisplayName(Literal.BINARY)
    BINARY {
        @Override
        MimeType mapped() {
            return MimeType.BINARY;
        }
    },
    @DisplayName(Literal.APPLICATION_JSON)
    APPLICATION_JSON {
        @Override
        MimeType mapped() {
            return MimeType.APPLICATION_JSON;
        }
    },
    @DisplayName(Literal.APPLICATION_JAVA)
    APPLICATION_JAVA {
        @Override
        MimeType mapped() {
            return MimeType.APPLICATION_JAVA;
        }
    },
    @DisplayName(Literal.MULTIPART_RELATED)
    MULTIPART_RELATED {
        @Override
        MimeType mapped() {
            return MimeType.MULTIPART_RELATED;
        }
    },
    @DisplayName(Literal.MULTIPART_FORM_DATA)
    MULTIPART_FORM_DATA {
        @Override
        MimeType mapped() {
            return MimeType.MULTIPART_FORM_DATA;
        }
    },
    @DisplayName(Literal.MULTIPART_X_MIXED_REPLACE)
    MULTIPART_X_MIXED_REPLACE {
        @Override
        MimeType mapped() {
            return MimeType.MULTIPART_X_MIXED_REPLACE;
        }
    };

    abstract MimeType mapped();

}
