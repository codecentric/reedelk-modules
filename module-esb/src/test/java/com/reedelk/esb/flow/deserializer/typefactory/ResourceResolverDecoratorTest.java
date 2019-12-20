package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceResolverDecoratorTest {

    private final long testModuleId = 10L;
    private final TypeFactoryContext factoryContext = new TypeFactoryContext(testModuleId);

    @Mock
    private DeserializedModule mockDeSerializedModule;
    @Mock
    private Module mockModule;

    private ResourceResolverDecorator decorator;

    @BeforeEach
    void setUp() {
        decorator = new ResourceResolverDecorator(TypeFactory.getInstance(), mockDeSerializedModule, mockModule);
    }

}