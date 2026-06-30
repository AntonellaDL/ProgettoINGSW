package com.bugboard.frontend.services;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.bugboard.frontend.model.Issue;
import com.bugboard.frontend.model.User;
import com.bugboard.frontend.model.dto.CreateIssueRequest;
import com.bugboard.frontend.utils.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiService {

    //singleton pattern per garantire un'unica istanza del servizio API
    private static ApiService instance;

    private ApiService(){}

    public static synchronized ApiService getInstance(){
        if(instance == null) instance = new ApiService();
        return instance;
    }

    //costanti per le chiamate API
    private static final String BASE_URL = "http://localhost:8080/api";
    // dati mock usati solo per login e la registrazione 
    private static final List<User>  users = new ArrayList<>();
    private static final Map<String, String> mockPasswordVault = new HashMap<>();
    private static final List<Issue> issues = new ArrayList<>();

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

    /*
    * Creazione issue con chiamata HTTP reale al backend
    */
    public boolean createIssue(CreateIssueRequest request) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            System.err.println("Errore: utente non autenticato");
            return false;
        }
        try{
            User currentUser = SessionManager.getInstance().getCurrentUser();
           // costruisce il JSON della parte data
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> jsonMap = new LinkedHashMap<>();
            jsonMap.put("title", request.getTitle());
            jsonMap.put("description", request.getDescription()); 
            jsonMap.put("issueType", request.getType());

            //priorità è opzionale, quindi la aggiungiamo solo se non è null
            if(request.getPriority() != null && !request.getPriority().isBlank()){
                jsonMap.put("priority", request.getPriority());
            }

            jsonMap.put("creatorID",currentUser.getId());
            String jsonData = mapper. writeValueAsString(jsonMap);

            //genera un boundary unico per il multipart
            String boundary ="Bug Board Boundary"+ UUID.randomUUID().toString().replace("-", "");
            String CRLF = "\r\n";

            //costruisce il corpo 

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //parte data

            baos.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"data\"" + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Type: application/json" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(jsonData.getBytes(StandardCharsets.UTF_8));
            baos.write(CRLF.getBytes(StandardCharsets.UTF_8));

            //parte attachmentFile (immagine, opzionale )

            if(request.getImageData() != null && request.getImageData().length >0){
                String fileName = (request.getImageName()!= null && !request.getImageName().isBlank())
                ? request.getImageName() : "image.jpg" ;
                baos.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
                baos.write(("Content-Disposition: form-data; name=\"attachmentFile\"; filename=\"" + fileName + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
                baos.write(request.getImageData());
                baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
            }

            //chiude il corpo
            baos.write(("--"+ boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
            
            // invia richiesta http
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri (URI.create(BASE_URL + "/issues"))
                .header("Content-Type", "multipart/form-data; boundary="+ boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("[aPI] create issue -> HTTP "+ response.statusCode());
            if(response.statusCode()!= 200){
                System.err.println("[API] Risposta backend: "+ response.body());
            }
            return response.statusCode()==200;

        }catch (Exception e){
            System.err.println("[API] Errore durante la creazione della issue: "+ e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // METODI GESTIONE COMMENTI

    /*
    * Invia un nuovo commento al backend tramite POST /api/comments
    * In precedenza i commenti venivano aggiunti solo in memoria lato
    * frontend e non venivano mai persistiti sul server: questo metodo
    * colma quella mancanza.
    */
    public boolean postComment(Long issueId, Long authorId, String text){
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> jsonMap = new LinkedHashMap<>();
            jsonMap.put("issueId", issueId);
            jsonMap.put("authorId", authorId);
            jsonMap.put("text", text);
            String jsonData = mapper.writeValueAsString(jsonMap);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/comments"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("[API] postComment -> HTTP " + response.statusCode());
            if(response.statusCode() != 200){
                System.err.println("[API] Risposta backend: " + response.body());
            }
            return response.statusCode() == 200;
        }catch(Exception e){
            System.err.println("[API] Errore durante l'invio del commento: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //aggiornamento stato 
    /*
    * * Chiama PUT /api/issues/{issueId}/status?status={newStatus}&userId={userId}
    ** il backend verifica che @code userId sia l'assegnatario 
    */
    public boolean updateIssueStatus(String issueId, String newStatus,Long userId){
        try{
            String url = BASE_URL + "/issues/"+ issueId+"/status?status="+newStatus+"&userId="+userId;
            HttpClient client= HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .PUT(HttpRequest.BodyPublishers.noBody())
            .build();

            HttpResponse<String> response =client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[API] update Issue status "+ response.statusCode());
            if(response.statusCode()==403){
                System.out.println("[API] Accesso negato: non sei l'assegnatario.");
                
            } else if (response.statusCode() != 200) {
                System.err.println("[API] Risposta backend: " + response.body());                }
            
                return response.statusCode() == 200;
            
        } catch (Exception e) {
                    System.err.println("[API] Errore updateIssueStatus: " + e.getMessage());
                    e.printStackTrace();
                    return false;
        }
    }        
}
