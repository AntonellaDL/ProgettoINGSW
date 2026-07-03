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
import com.bugboard.frontend.model.Comment;
import com.bugboard.frontend.model.Notification;

public class ApiService {

    // singleton pattern per garantire un'unica istanza del servizio API
    private static ApiService instance;

    // costanti per le chiamate API
    private static final String BASE_URL = "http://localhost:8080/api";
    
    // Istanze uniche e globali 
    private final HttpClient client;
    private final ObjectMapper mapper;

    private ApiService(){
        // Vengono creati una sola volta all'avvio
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();

        this.mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static synchronized ApiService getInstance(){
        if(instance == null) instance = new ApiService();
        return instance;
    }

    // METODI DI AUTENTICAZIONE E GESTIONE UTENTI 
    public boolean loginFrame(String email, String password){ 
        try {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", email); 
            credentials.put("password", password);
            
            // Usiamo il mapper globale
            String jsonData = mapper.writeValueAsString(credentials);

            // Usiamo il client globale
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login")) 
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                User loggedUser = mapper.readValue(response.body(), User.class);
                
                SessionManager.getInstance().setAuthToken("token-" + loggedUser.getId()); 
                SessionManager.getInstance().setCurrentUser(loggedUser);
                
                System.out.println("[API] Login effettuato dal server per: " + loggedUser.getEmail());
                return true;
            } else if (response.statusCode() == 401) {
                System.err.println("[API] Credenziali errate. Rifiutato dal server.");
                return false;
            } else {
                System.err.println("[API] Errore imprevisto. Status: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("[API] Errore di connessione per il login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerNewUser(String email, String password, String role) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            System.err.println("[API] Errore: Utente non loggato tenta di creare una utenza.");
            return false;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            System.err.println("[API] Accesso Negato: Solo l'ADMIN può creare utenze.");
            return false;
        }

        try {
            Map<String, String> newUserPayload = new HashMap<>();
            newUserPayload.put("email", email);
            newUserPayload.put("password", password);
            
            String backendRole = "USER"; 
            if (role != null && (role.toUpperCase().contains("ADMIN") || role.toUpperCase().contains("AMMINISTRATORE"))) {
                backendRole = "ADMIN";
            }
            newUserPayload.put("role", backendRole);
            newUserPayload.put("name", email.contains("@") ? email.split("@")[0] : email);

            String jsonData = mapper.writeValueAsString(newUserPayload);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users")) 
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("[API] Utente creato con successo sul server! Email: " + email + " | Ruolo: " + role);
                return true;
            } else {
                System.err.println("[API] Errore dal server durante la registrazione. HTTP " + response.statusCode());
                System.err.println("Dettaglio errore: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("[API] Errore di connessione durante la registrazione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            return new ArrayList<>();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users"))
                .GET()
                .build();
                
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), new com.fasterxml.jackson.core.type.TypeReference<List<User>>(){});
            } else {
                System.err.println("[API] Errore caricamento utenti. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[API] Impossibile connettersi per caricare gli utenti: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // METODI GESTIONE ISSUE 
    public List<Issue> getIssues(){
        if(!SessionManager.getInstance().isLoggedIn()){
            System.err.println("Errore: Utente non loggato sta provando a leggere le issues");
            return new ArrayList<>();
        }
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/issues"))
                .GET()
                .build();
                
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), new com.fasterxml.jackson.core.type.TypeReference<List<Issue>>(){});
            } else {
                System.err.println("[API] Errore nel caricamento delle issue. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[API] Impossibile contattare il backend per le issue: " + e.getMessage());
        }
        
        return new ArrayList<>(); 
    }

    public boolean createIssue(CreateIssueRequest request) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            System.err.println("Errore: utente non autenticato");
            return false;
        }
        try{
            User currentUser = SessionManager.getInstance().getCurrentUser();
            Map<String,Object> jsonMap = new LinkedHashMap<>();
            jsonMap.put("title", request.getTitle());
            jsonMap.put("description", request.getDescription()); 
            jsonMap.put("issueType", request.getType());

            if(request.getPriority() != null && !request.getPriority().isBlank()){
                jsonMap.put("priority", request.getPriority());
            }

            jsonMap.put("creatorID",currentUser.getId());
            String jsonData = mapper.writeValueAsString(jsonMap);

            String boundary ="Bug Board Boundary"+ UUID.randomUUID().toString().replace("-", "");
            String CRLF = "\r\n";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            baos.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"data\"" + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Type: application/json" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(jsonData.getBytes(StandardCharsets.UTF_8));
            baos.write(CRLF.getBytes(StandardCharsets.UTF_8));

            if(request.getImageData() != null && request.getImageData().length >0){
                String fileName = (request.getImageName()!= null && !request.getImageName().isBlank())
                ? request.getImageName() : "image.jpg" ;
                baos.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
                baos.write(("Content-Disposition: form-data; name=\"attachmentFile\"; filename=\"" + fileName + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
                baos.write(request.getImageData());
                baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
            }

            baos.write(("--"+ boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri (URI.create(BASE_URL + "/issues"))
                .header("Content-Type", "multipart/form-data; boundary="+ boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("[API] create issue -> HTTP "+ response.statusCode());
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
    public boolean postComment(Long issueId, Long authorId, String text){
        try{
            Map<String,Object> jsonMap = new LinkedHashMap<>();
            jsonMap.put("issueId", issueId);
            jsonMap.put("authorId", authorId);
            jsonMap.put("text", text);
            
            String jsonData = mapper.writeValueAsString(jsonMap);

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

    public List<Comment> getCommentsByIssue(String issueId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/comments/issue/" + issueId))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), new com.fasterxml.jackson.core.type.TypeReference<List<Comment>>(){});
            } else {
                System.err.println("[API] Impossibile recuperare i commenti. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[API] Errore di rete in getCommentsByIssue: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>(); 
    }

    public boolean updateIssueStatus(String issueId, String newStatus,Long userId){
        try{
            String url = BASE_URL + "/issues/"+ issueId+"/status?status="+newStatus+"&userId="+userId;
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .PUT(HttpRequest.BodyPublishers.noBody())
            .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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
    
    public boolean assignIssue(String issueId, Long assigneeId, Long currentUserId) {
        try {
            String url = BASE_URL + "/issues/" + issueId + "/assign?assigneeId=" + assigneeId + "&userId=" + currentUserId;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.noBody()) 
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[API] assign Issue -> HTTP " + response.statusCode());
            
            if (response.statusCode() != 200) {
                System.err.println("[API] Errore assegnazione backend: " + response.body());
            }
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            System.err.println("[API] Errore di connessione durante assignIssue: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // METODI GESTIONE NOTIFICHE
    
    /**
     * Recupera tutte le notifiche di un utente.
     */
    public List<Notification> getUserNotifications(Long userId) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            return new ArrayList<>();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/notifications/user/" + userId))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), new com.fasterxml.jackson.core.type.TypeReference<List<Notification>>(){});
            } else {
                System.err.println("[API] Impossibile caricare le notifiche. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[API] Errore di connessione per getUserNotifications: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Segna una specifica notifica come letta.
     */
    public boolean markNotificationAsRead(Long notificationId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/notifications/" + notificationId + "/read"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println("[API] Notifica " + notificationId + " segnata come letta.");
                return true;
            } else {
                System.err.println("[API] Errore segnando notifica come letta. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[API] Errore di rete in markNotificationAsRead: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}