package com.reedelk.rest.commons;

import java.util.Objects;

public abstract class BiKey<T1,T2> {

    private final T1 value1;
    private final T2 value2;

    public BiKey(T1 value1, T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiKey<?, ?> biKey = (BiKey<?, ?>) o;
        return value1.equals(biKey.value1) &&
                value2.equals(biKey.value2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value1, value2);
    }

    public T1 getValue1() {
        return value1;
    }

    public T2 getValue2() {
        return value2;
    }
}
