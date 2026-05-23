package com.bugboard.frontend.utils;

public class PasswordValidator {

    // Costruttore privato per impedire l'istanziamento (new PasswordValidator())
    private PasswordValidator() {}

    /**
     * Valida la password secondo le policy di sicurezza attuali.
     * Attualmente richiede solo una lunghezza minima di 6 caratteri per facilitare i test
     */
    public static boolean isValid(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // Controllo "soft": Almeno 6 caratteri
        return password.length() >= 6;

        // versione piu rigida
        // Richiede: min 8 caratteri, 1 Maiuscola, 1 minuscola, 1 numero
        // =========================================================
        // String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        // return password.matches(passwordRegex);
        
    }
}