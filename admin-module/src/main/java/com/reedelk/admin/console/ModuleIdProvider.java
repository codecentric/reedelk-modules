package com.reedelk.admin.console;

import com.reedelk.runtime.api.file.ModuleId;

public class ModuleIdProvider {

    static long id;

    public static ModuleId get()  {
        return () -> id;
    }
}
