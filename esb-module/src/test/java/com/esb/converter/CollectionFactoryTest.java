package com.esb.converter;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unchecked")
class CollectionFactoryTest {

    @Test
    void shouldThrowExceptionIfUnsupportedCollectionType() {
        // Expect
        assertThrows(IllegalArgumentException.class, () -> {
            CollectionFactory.from(Number.class);
        });
    }

    @Test
    void shouldReturnCorrectImplementationForCollection() {
        // When
        Collection actual = CollectionFactory.from(Collection.class);

        // Then
        assertThat(actual).isInstanceOf(ArrayList.class);
    }

    @Test
    void shouldReturnCorrectImplementationForList() {
        // When
        Collection actual = CollectionFactory.from(List.class);

        // Then
        assertThat(actual).isInstanceOf(ArrayList.class);
    }

    @Test
    void shouldReturnCorrectImplementationForSet() {
        // When
        Collection actual = CollectionFactory.from(Set.class);

        // Then
        assertThat(actual).isInstanceOf(HashSet.class);
    }
}
