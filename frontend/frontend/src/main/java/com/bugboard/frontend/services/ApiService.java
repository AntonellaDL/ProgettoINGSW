package com.bugboard.frontend.services;

import java.util.ArrayList;
import java.util.List;

import com.bugboard.frontend.model.Issue;
import com.bugboard.frontend.model.User;
import com.bugboard.frontend.model.dto.CreateIssueRequest;
import com.bugboard.frontend.utils.SessionManager;

public class ApiService {
    private static ApiService instance;

    private ApiService(){}

    public static synchronized ApiService getInstance(){
        if(instance == null) instance = new ApiService();
        return instance;
    }

    //dati di prova

    private static List<Issue> issues = new ArrayList<>();
    static{
        issues.add(new Issue("1","Login lento","Il login ci mette troppo", "todo", "bug", "alta", null, null, null));
        issues.add(new Issue("2", "Errore 500", "Errore server quando si salva", "in progress", "bug", "media", null, null, null));
        issues.add(new Issue("3", "Migliorare UI", "Rendere l'interfaccia user-friendly", "done", "enhancement", "bassa", null, null, null));
    }

    public boolean LoginFrame(String email, String password){
        //simulo controllo credenziali
        if(email.equals("admin")&& password.equals("password")){

            //creo utente finto

            User fakeUser = new User();
            fakeUser.setId(1L);
            fakeUser.setEmail(email);
            fakeUser.setName("Amministratore Mock");
            fakeUser.setRole("ADMIN");

            //simulo token
            String fakeToken = "mock-token-abc-123";

            //salvo tutto in session manager
            SessionManager.getInstance().setAuthToken(fakeToken);
            SessionManager.getInstance().setCurrentUser(fakeUser);

            return true;
        }
        return false;
    }

    public List<Issue> getIssues(){
        //controllo di sicurezza simulato

        if(!SessionManager.getInstance().isLoggedIn()){
            System.err.println("Errore: Utente non loggato sta provando a leggere le issues");
            return new ArrayList<>();
        }
        return new ArrayList<>(issues);
    }

    public boolean createIssue(CreateIssueRequest request) {
    if (!SessionManager.getInstance().isLoggedIn()) {
        return false;
    }

    try {
        Issue newIssue = new Issue();
        newIssue.setTitle(request.getTitle());
        newIssue.setDescription(request.getDescription());
        newIssue.setType(request.getType());
        newIssue.setPriority(request.getPriority());
        
        newIssue.setId(String.valueOf(issues.size() + 1));
        newIssue.setStatus("TODO");
        
        // Aggiungo alla lista
        issues.add(newIssue);
        System.out.println("MOCK SERVER: Creata issue '" + request.getTitle() + "'");
        
        return true;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }

    }
}