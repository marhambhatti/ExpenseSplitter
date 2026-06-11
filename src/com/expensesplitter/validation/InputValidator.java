// FIXED: Updated email regex and added isPositiveDouble per spec
package com.expensesplitter.validation;

// Input Validation
public class InputValidator {
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Email Verify
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isPositiveDouble(String s) {
        try { return Double.parseDouble(s) > 0; }
        catch (NumberFormatException e) { return false; }
    }
}
