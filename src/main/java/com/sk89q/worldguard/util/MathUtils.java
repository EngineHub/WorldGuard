/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.util;

/**
 * Math-related utilities.
 */
public final class MathUtils {

    private MathUtils() {
    }

    private static void checkNoOverflow(boolean condition) {
        if (!condition) {
            throw new ArithmeticException("overflow");
        }
    }

    /**
     * Returns the product of {@code a} and {@code b}, provided it does not overflow.
     *
     * <p>Borrowed from Google Guava since Bukkit uses an old version.</p>
     *
     * @throws ArithmeticException if {@code a * b} overflows in signed {@code long} arithmetic
     */
    public static long checkedMultiply(long a, long b) {
        // Hacker's Delight, Section 2-12
        int leadingZeros = Long.numberOfLeadingZeros(a) + Long.numberOfLeadingZeros(~a)
                + Long.numberOfLeadingZeros(b) + Long.numberOfLeadingZeros(~b);
        /*
         * If leadingZeros > Long.SIZE + 1 it's definitely fine, if it's < Long.SIZE it's definitely
         * bad. We do the leadingZeros check to avoid the division below if at all possible.
         *
         * Otherwise, if b == Long.MIN_VALUE, then the only allowed values of a are 0 and 1. We take
         * care of all a < 0 with their own check, because in particular, the case a == -1 will
         * incorrectly pass the division check below.
         *
         * In all other cases, we check that either a is 0 or the result is consistent with division.
         */
        if (leadingZeros > Long.SIZE + 1) {
            return a * b;
        }
        checkNoOverflow(leadingZeros >= Long.SIZE);
        checkNoOverflow(a >= 0 | b != Long.MIN_VALUE);
        long result = a * b;
        checkNoOverflow(a == 0 || result / a == b);
        return result;
    }

}
