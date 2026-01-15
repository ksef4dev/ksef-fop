/*
 * SPDX-License-identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

public final class Strings {

    /**
     * Returns the default string if the input string is null or empty.
     *
     * @param str        the string to check
     * @param defaultStr the default string to return if str is {@code null} or empty
     * @return {@code defaultStr} if {@code str} is {@code null} or empty; otherwise, returns {@code str}
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return str == null || str.isEmpty() ? defaultStr : str;
    }

    private Strings() {
        // utility class
    }
}
