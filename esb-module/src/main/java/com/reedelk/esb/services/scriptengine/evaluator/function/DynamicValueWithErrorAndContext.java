package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;

public class DynamicValueWithErrorAndContext implements FunctionDefinitionBuilder<DynamicValue> {

    private static final String TEMPLATE =
            "function %s(error, context) {\n" +
                    "  return %s\n" +
                    "};";

    @Override
    public String from(ScriptBlock scriptBlock) {
        DynamicValue dynamicValue = (DynamicValue) scriptBlock;
        String functionBody = ScriptUtils.unwrap(dynamicValue.body());
        return String.format(TEMPLATE, dynamicValue.functionName(), functionBody);
    }
}
