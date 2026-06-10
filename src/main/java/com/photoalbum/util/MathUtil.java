/*
    Class Name: MathUtil
    Description: Provides reusable mathematical helper methods.
    Date Created: 2026-06-10
*/

package com.photoalbum.util;

/**
 * Mathematical utility functions
 */
public class MathUtil {

    /**
     * Calculate the Greatest Common Divisor (GCD) of two integers
     * @param a First integer
     * @param b Second integer
     * @return The GCD of a and b
     */
    public static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Calculate the sum of two integers
     * @param a First integer
     * @param b Second integer
     * @return The sum of a and b
     */
    public static int sum(int a, int b) {
        return a + b;
    }

    public static int min(int a, int b) {
        return a < b ? a : b;
    }

}