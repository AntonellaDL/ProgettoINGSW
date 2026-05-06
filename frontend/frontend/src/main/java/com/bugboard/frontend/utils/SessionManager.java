package com.bugboard.frontend.utils;

import com.bugboard.frontend.model.User;

public class SessionManager {

    //istanza statica perche' appartiene alla classe e non all'oggetto specifico
    //uno in tutta l'app
    public static SessionManager instance;

    //dati da conservare in memoria
    private String authToken;
    private User currentUser;

    //fondamentale per impendire a chiunque di fare new session...
    private SessionManager(){}

    //metodo di accesso
    public static synchronized SessionManager getInstance(){
        if(instance == null){
            instance = new SessionManager();
        }
        return instance;
    }

    public String getAuthToken(){
        return authToken;
    }

    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }

    public User getCurrentUser(){
        return currentUser;
    }

    public void setCurrentUser(User currentUser){
        this.currentUser = currentUser;
    }


    //controlla se c'e' un utente loggato correttamente

    public boolean isLoggedIn(){
        return authToken != null && currentUser != null;
    }

    //nel momento in  cui si preme logout pulisce il tutto

    public void logout(){
        this.authToken = null;
        this.currentUser = null;
        System.err.println("Sessione terminata.");
    }

    //helper per UI dice subito se admin, utile per nascondere/mostrare bottoni in Dashboard

    public boolean isAdmin(){
        if(currentUser == null) return false;

        return "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }


}