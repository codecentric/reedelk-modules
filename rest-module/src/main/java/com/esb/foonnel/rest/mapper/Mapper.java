package com.esb.foonnel.rest.mapper;

public interface Mapper<I,O> {

    O map(I input);

}
