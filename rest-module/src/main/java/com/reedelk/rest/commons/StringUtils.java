package com.reedelk.rest.commons;

public class StringUtils {

    public static boolean isBlank(final CharSequence sequence) {
        if (sequence == null) return true;

        int sequenceLength = sequence.length();
        if (sequenceLength == 0) return true;

        for (int i = 0; i < sequenceLength; i++) {
            if (!Character.isWhitespace(sequence.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence sequence) {
        return !isBlank(sequence);
    }
}
