package com.bugboard.frontend.services;

public class AuthService {
    
    public boolean login(String email, String password) {
        // Simulazione di una chiamata API per il login
        boolean success = ApiService.getInstance().LoginFrame(email, password);
        
        if (success) {
            System.out.println("[AuthService] Login effettuato con successo via ApiService per: " + email);
            return true;
        }

        System.out.println("[AuthService] Login fallito per: " + email);
        return false;
    }    

    // Permette al CreateUserDialog di registrare un utente 
    public boolean register(String email, String password, String role) {
        // Passo la richiesta al server mock in ApiService
        return ApiService.getInstance().registerNewUser(email, password, role);
    }
}
