package com.reedelk.file.component;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.AbstractInbound;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ESBComponent("Directory listener")
@Component(service = DirectoryListener.class, scope = ServiceScope.PROTOTYPE)
public class DirectoryListener extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryListener.class);

    @Property("Directory path")
    private String directoryPath;


    @Override
    public void onStart() {
    }

    @Override
    public void onShutdown() {

    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }
}
