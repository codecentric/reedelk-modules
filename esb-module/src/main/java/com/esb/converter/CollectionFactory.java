package com.esb.converter;

import java.util.*;

import static com.esb.commons.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class CollectionFactory {

    private static final Collection<Class> SUPPORTED_COLLECTIONS = asList(Set.class, List.class, Collection.class);

    private CollectionFactory() {
    }

    public static Collection from(Class collectionClass) {
        checkArgument(SUPPORTED_COLLECTIONS.contains(collectionClass), format("Collection class %s not supported", collectionClass.getName()));

        if (collectionClass == Set.class) return new HashSet();
        if (collectionClass == List.class) return new ArrayList();
        if (collectionClass == Collection.class) return new ArrayList();

        // Should never get to this point (unless SUPPORTED_COLLECTION does not reflect this method implementation)
        throw new IllegalArgumentException(format("Collection class %s not supported", collectionClass.getName()));
    }

    public static <C> boolean isSupported(Class<C> clazz) {
        return SUPPORTED_COLLECTIONS.contains(clazz);
    }
}
