package com.nivesh.library.util;

/**
 * Utility methods for generating and validating Luhn check digits.
 */
public class LuhnUtil {

    /** Computes the check digit that should be appended to the supplied number. */
    public static int computeLastDigit(String number) {
        int sum = luhnSum(number, true);
        return (10 - (sum % 10)) % 10;
    }

    /** Validates that a number containing a Luhn check digit is internally consistent. */
    public static boolean validateNumber(String number) {
        return luhnSum(number, false) % 10 == 0;
    }

    /** Calculates the alternating-double Luhn checksum. */
    private static int luhnSum(String digits, boolean doubleRightMost) {
        int sum = 0;
        boolean doubleIt = doubleRightMost;

        for (int i = digits.length()-1; i>=0; i--) {
            int digit = digits.charAt(i) - '0';
            if (doubleIt) {
                digit = digit * 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }
            sum = sum + digit;
            doubleIt = !doubleIt;
        }
        return sum;
    }
}
