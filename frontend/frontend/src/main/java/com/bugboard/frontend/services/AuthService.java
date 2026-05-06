package com.bugboard.frontend.services;

public class AuthService {
    public boolean login(String email, String password){
        if ("admin".equals(email) && "password".equals(password)){
            System.out.println("Login effettuato come admin");
            return true;
        }

        System.out.println("Login fallito per email: " + email);
        return false;
    }    
}
