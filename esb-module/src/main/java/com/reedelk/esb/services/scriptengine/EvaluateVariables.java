package com.reedelk.esb.services.scriptengine;

import static java.lang.System.lineSeparator;

/**
 * It builds:
 *
 * var var1 = 'my first var';
 * var var2 = 'my second var';
 * var result = {var1:var1,var2:var2};
 * result;
 */
class EvaluateVariables {

    private final VariableAssignment[] variables;

    public EvaluateVariables(VariableAssignment ...variables) {
        this.variables = variables;
    }

    public static EvaluateVariables all(VariableAssignment ...variables) {
        return new EvaluateVariables(variables);
    }

    public String script() {
        StringBuilder result = new StringBuilder();
        result.append("var result = {");
        StringBuilder builder = new StringBuilder();
        for (VariableAssignment variable : variables) {
            builder.append(variable.script())
                    .append(lineSeparator());
            result.append(variable.name())
                    .append(":")
                    .append(" ")
                    .append(variable.name())
                    .append(",")
                    .append(" ");
        }
        // remove comma
        if (variables.length > 0) {
            result.delete(result.length() - 2, result.length());
        }
        result.append("};")
                .append(lineSeparator())
                .append("result;");
        return builder.append(result).toString();
    }

}