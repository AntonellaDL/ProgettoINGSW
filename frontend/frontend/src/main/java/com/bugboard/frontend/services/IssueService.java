package com.bugboard.frontend.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import  java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.bugboard.frontend.model.dto.CreateIssueRequest;
import com.bugboard.frontend.utils.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class IssueService {
    private static final String BASE_URL = "http://localhost:8080/api/issues";
     // Stringa di separazione univoca per le parti multipart
    private static final String BOUNDARY = "----BugBoardBoundary8f2a1";
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient   client = HttpClient.newHttpClient();
    
    /**
      * Invia la nuova issue al backend tramite POST multipart/form-data.
      *
      * @return true se il server ha risposto 200 o 201, false altrimenti
      */
    public boolean createIssue(CreateIssueRequest request) {
        try {
            String token  = SessionManager.getInstance().getAuthToken();
            Long   userId = SessionManager.getInstance().getCurrentUser().getId();
            ObjectNode jsonNode = mapper.createObjectNode();
            jsonNode.put("title",         request.getTitle());
            jsonNode.put("description",   request.getDescription());
            jsonNode.put("issueType",     request.getType());
            jsonNode.put("issuePriority", request.getPriority() != null ? request.getPriority() : ""); 
            jsonNode.put("creatorId",     userId);
    
            String jsonData = mapper.writeValueAsString(jsonNode);
             // Costruisce il corpo multipart completo
            byte[] body = buildMultipartBody(jsonData, request.getImageData(), request.getImageName());
            
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(BASE_URL))
                    .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
    
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    
            System.out.println("createIssue → HTTP " + response.statusCode());
            return response.statusCode() == 200 || response.statusCode() == 201;
    
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
      * Costruisce il body grezzo di una richiesta multipart/form-data */
    private byte[] buildMultipartBody(String jsonData,byte[] imageBytes,String imageName) throws IOException {
    
        ByteArrayOutputStream out = new ByteArrayOutputStream();
    
        writeLine(out, "--" + BOUNDARY);
        writeLine(out, "Content-Disposition: form-data; name=\"data\"");
        writeLine(out, "Content-Type: application/json");
        writeLine(out, ""); // riga vuota obbligatoria prima del contenuto
        writeLine(out, jsonData);
    
        if (imageBytes != null && imageBytes.length > 0 && imageName != null) {
            writeLine(out, "--" + BOUNDARY);
            writeLine(out, "Content-Disposition: form-data;"+ " name=\"attachmentFile\"; filename=\"" + imageName + "\"");
            writeLine(out, "Content-Type: application/octet-stream");
            writeLine(out, ""); // riga vuota obbligatoria prima del contenuto
            out.write(imageBytes);  // bytes binari senza codifica
            writeLine(out, "");     // \r\n finale dopo i byte
        }

        writeLine(out, "--" + BOUNDARY + "--");
        return out.toByteArray();
    }
    
    private void writeLine(ByteArrayOutputStream out, String line) throws IOException {
        out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
    }
    

}