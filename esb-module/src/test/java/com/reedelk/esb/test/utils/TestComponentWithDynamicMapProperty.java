package com.reedelk.esb.test.utils;

import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicmap.*;

public class TestComponentWithDynamicMapProperty implements ProcessorSync {

    private DynamicLongMap dynamicLongMapProperty;
    private DynamicFloatMap dynamicFloatMapProperty;
    private DynamicDoubleMap dynamicDoubleMapProperty;
    private DynamicStringMap dynamicStringMapProperty;
    private DynamicBooleanMap dynamicBooleanMapProperty;
    private DynamicIntegerMap dynamicIntegerMapProperty;
    private DynamicBigDecimalMap dynamicBigDecimalMapProperty;
    private DynamicBigIntegerMap dynamicBigIntegerMapProperty;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException("Test Only ProcessorSync");
    }

    public DynamicLongMap getDynamicLongMapProperty() {
        return dynamicLongMapProperty;
    }

    public void setDynamicLongMapProperty(DynamicLongMap dynamicLongMapProperty) {
        this.dynamicLongMapProperty = dynamicLongMapProperty;
    }

    public DynamicFloatMap getDynamicFloatMapProperty() {
        return dynamicFloatMapProperty;
    }

    public void setDynamicFloatMapProperty(DynamicFloatMap dynamicFloatMapProperty) {
        this.dynamicFloatMapProperty = dynamicFloatMapProperty;
    }

    public DynamicDoubleMap getDynamicDoubleMapProperty() {
        return dynamicDoubleMapProperty;
    }

    public void setDynamicDoubleMapProperty(DynamicDoubleMap dynamicDoubleMapProperty) {
        this.dynamicDoubleMapProperty = dynamicDoubleMapProperty;
    }

    public DynamicStringMap getDynamicStringMapProperty() {
        return dynamicStringMapProperty;
    }

    public void setDynamicStringMapProperty(DynamicStringMap dynamicStringMapProperty) {
        this.dynamicStringMapProperty = dynamicStringMapProperty;
    }

    public DynamicBooleanMap getDynamicBooleanMapProperty() {
        return dynamicBooleanMapProperty;
    }

    public void setDynamicBooleanMapProperty(DynamicBooleanMap dynamicBooleanMapProperty) {
        this.dynamicBooleanMapProperty = dynamicBooleanMapProperty;
    }

    public DynamicIntegerMap getDynamicIntegerMapProperty() {
        return dynamicIntegerMapProperty;
    }

    public void setDynamicIntegerMapProperty(DynamicIntegerMap dynamicIntegerMapProperty) {
        this.dynamicIntegerMapProperty = dynamicIntegerMapProperty;
    }

    public DynamicBigDecimalMap getDynamicBigDecimalMapProperty() {
        return dynamicBigDecimalMapProperty;
    }

    public void setDynamicBigDecimalMapProperty(DynamicBigDecimalMap dynamicBigDecimalMapProperty) {
        this.dynamicBigDecimalMapProperty = dynamicBigDecimalMapProperty;
    }

    public DynamicBigIntegerMap getDynamicBigIntegerMapProperty() {
        return dynamicBigIntegerMapProperty;
    }

    public void setDynamicBigIntegerMapProperty(DynamicBigIntegerMap dynamicBigIntegerMapProperty) {
        this.dynamicBigIntegerMapProperty = dynamicBigIntegerMapProperty;
    }
}
