/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.internal.util;


import org.checkerframework.checker.nullness.qual.NonNull;

public final class AssertionUtil {

    private AssertionUtil() {
        // utility class
    }

    public static int requireOver1(String name, final int value) {
        if (value < 1) {
            throw mustBe(name, value, ">= 1");
        }
        return value;
    }

    public static int requireNonNegative(String name, int value) {
        if (value < 0) {
            throw mustBe(name, value, "non-negative");
        }
        return value;
    }

    public static RuntimeException mustBe(String name, Object value, String condition) {
        return new IllegalArgumentException(String.format("%s must be %s, got %s", name, condition, value));
    }

    @NonNull
    public static <T> T requireParamNotNull(String paramName, T obj) {
        if (obj == null) {
            throw new NullPointerException("Parameter " + paramName + " is null");
        }

        return obj;
    }
}
