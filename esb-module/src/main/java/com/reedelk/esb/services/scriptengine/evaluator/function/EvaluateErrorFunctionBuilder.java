package com.reedelk.esb.services.scriptengine.evaluator.function;

public class EvaluateErrorFunctionBuilder implements FunctionBuilder {

    private static final String EVALUATE_ERROR_FUNCTION =
            "var %s = function(error, context) {\n" +
                    "  return %s\n" +
                    "};";

    @Override
    public String build(String functionName, String functionBody) {
        return String.format(EVALUATE_ERROR_FUNCTION, functionName, functionBody);
    }
}
