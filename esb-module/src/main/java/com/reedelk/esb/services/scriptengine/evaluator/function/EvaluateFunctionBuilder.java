package com.reedelk.esb.services.scriptengine.evaluator.function;

public class EvaluateFunctionBuilder implements FunctionBuilder {

    private static final String EVALUATE_FUNCTION =
            "var %s = function(message, context) {\n" +
                    "  return %s\n" +
                    "};";

    @Override
    public String build(String functionName, String functionBody) {
        return String.format(EVALUATE_FUNCTION, functionName, functionBody);
    }
}