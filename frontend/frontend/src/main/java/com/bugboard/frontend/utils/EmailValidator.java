package com.bugboard.frontend.utils;

import java.util.regex.Pattern;

public class EmailValidator {

    private static final String EMAIL_PATTERN = 
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

    // Costruttore privato per impedire di fare new EmailValidator()
    private EmailValidator() {}

    /**
     * Valida l'email passata come parametro
     * * @param email l'email da validare
     * @return true se l'email è valida, false altrimenti
     */
    public static boolean isValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return PATTERN.matcher(email).matches();
    }
}
