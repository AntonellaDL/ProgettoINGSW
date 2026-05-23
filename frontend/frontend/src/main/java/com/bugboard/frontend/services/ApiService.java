package com.bugboard.frontend.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // STRUTTURE DATI IN MEMORIA 
    private static List<User> users = new ArrayList<>();
    // Mappa privata per le password
    private static Map<String, String> mockPasswordVault = new HashMap<>(); 
    
    private static List<Issue> issues = new ArrayList<>();

    static {
        // Popolamento Issue
        issues.add(new Issue("1","Login lento","Il login ci mette troppo", "todo", "BUG", "ALTA", null, null, null));
        issues.add(new Issue("2", "Errore 500", "Errore server quando si salva", "in_progress", "BUG", "MEDIA", null, null, null));
        issues.add(new Issue("3", "Migliorare UI", "Rendere l'interfaccia user-friendly", "done", "FEATURE", "BASSA", null, null, null));

        // Popolamento Amministratore di default
        User defaultAdmin = new User();
        defaultAdmin.setId(1L);
        defaultAdmin.setEmail("admin@bugboard.com"); 
        defaultAdmin.setName("Amministratore di Default");
        defaultAdmin.setRole("ADMIN");
        
        users.add(defaultAdmin);
        // Salvo la password nella cassaforte simulata
        mockPasswordVault.put("admin@bugboard.com", "password"); 
    }

    // METODI DI AUTENTICAZIONE E GESTIONE UTENTI 
    public boolean LoginFrame(String email, String password){
        for(User u : users) {
            if (u.getEmail().equals(email)) {
                
                // Recupero la password dalla mappa privata usando l'email
                String correctPassword = mockPasswordVault.get(email);
                
                if (correctPassword != null && correctPassword.equals(password)) {
                    SessionManager.getInstance().setAuthToken("mock-token-" + u.getEmail());
                    SessionManager.getInstance().setCurrentUser(u);
                    System.out.println("[MOCK SERVER] Login riuscito per utente: " + u.getEmail() + " | Ruolo: " + u.getRole());
                    return true;
                }
            }
        }
        System.out.println("[MOCK SERVER] Tentativo di login fallito per: " + email);
        return false;
    }

    public boolean registerNewUser(String email, String password, String role) {
        // Controllo se l'utente è loggato
        if (!SessionManager.getInstance().isLoggedIn()) {
            System.err.println("[MOCK SERVER] Errore: Utente non loggato tenta di creare una utenza.");
            return false;
        }

        // Controllo se l'utente ha il ruolo di ADMIN
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            System.err.println("[MOCK SERVER] Accesso Negato: L'utente " + currentUser.getEmail() + " non è ADMIN e non può creare utenze.");
            return false;
        }

        // Controllo se l'email è già registrata
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                System.err.println("[MOCK SERVER] Errore: L'email " + email + " è già registrata.");
                return false;
            }
        }

        // Creazione nuovo utente
        User newUser = new User();
        newUser.setId((long) (users.size() + 1));
        newUser.setEmail(email);
        newUser.setName(email.contains("@") ? email.split("@")[0] : email); 
        newUser.setRole(role.toUpperCase()); 

        users.add(newUser);
        
        // Salvataggio della password nella cassaforte simulata
        mockPasswordVault.put(email, password);
        
        System.out.println("[MOCK SERVER] Utente creato con successo! Email: " + email + " | Ruolo: " + role);
        return true;
    }

    // METODI GESTIONE ISSUE 
    public List<Issue> getIssues(){
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
            newIssue.setStatus("todo");
            
            issues.add(newIssue);
            System.out.println("[MOCK SERVER] Creata issue '" + request.getTitle() + "'");
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}