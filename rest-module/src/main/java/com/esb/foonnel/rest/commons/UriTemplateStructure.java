package com.esb.foonnel.rest.commons;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class UriTemplateStructure {

    private static final String EMPTY = "";
    private static final char END_VAR = '}';
    private static final char BEGIN_VAR = '{';
    private static final String DEFAULT_REGEXP = "([^/]*)";

    private final Collection<String> variableNames;
    private final Pattern pattern;

    private UriTemplateStructure(Collection<String> variableNames, Pattern pattern) {
        this.variableNames = variableNames;
        this.pattern = pattern;
    }

    public Collection<String> getVariableNames() {
        return variableNames;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public static UriTemplateStructure from(String template) {
        int depth = 0;
        List<String> variableNames = new ArrayList<>();
        StringBuilder pattern = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i < template.length(); i++) {
            char c = template.charAt(i);

            if (c == BEGIN_VAR) {
                depth++;
                if (depth == 1) {
                    // beginning of a defined URI variable
                    pattern.append(quote(builder));
                    builder = new StringBuilder();
                    continue;
                }

            } else if (c == END_VAR) {
                depth--;
                if (depth == 0) {
                    // end of a defined URI variable
                    String variable = builder.toString();
                    int idx = variable.indexOf(':');
                    if (idx == -1) {
                        pattern.append(DEFAULT_REGEXP);
                        variableNames.add(variable);

                    } else {
                        if (idx + 1 == variable.length()) {
                            throw new IllegalArgumentException(
                                    "custom regular expression must be specified after ':' for variable named \"" + variable + "\"");
                        }
                        String regex = variable.substring(idx + 1);
                        pattern.append('(');
                        pattern.append(regex);
                        pattern.append(')');
                        variableNames.add(variable.substring(0, idx));
                    }
                    builder = new StringBuilder();
                    continue;
                }
            }
            builder.append(c);
        }
        if (builder.length() > 0) {
            pattern.append(quote(builder));
        }

        return new UriTemplateStructure(variableNames, Pattern.compile(pattern.toString()));
    }

    private static String quote(StringBuilder builder) {
        return (builder.length() > 0 ? Pattern.quote(builder.toString()) : EMPTY);
    }
}
