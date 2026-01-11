/*
 * SPDX-License-identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

public final class Strings {

    /**
     * Returns the default string if the input string is null or blank.
     *
     * @param str        the string to check
     * @param defaultStr the default string to return if str is {@code null} or blank
     * @return {@code defaultStr} if {@code str} is {@code null} or blank; otherwise, returns {@code str}
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return str;
                }
            }
        }
        return defaultStr;
    }

    private Strings() {
        // utility class
    }
}
