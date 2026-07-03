package com.bugboard.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.dto.LoginRequest;
import com.bugboard.backend.model.dto.UserResponse; // Importante: usiamo il DTO sicuro
import com.bugboard.backend.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

  // Tramite UserService controllo le password nel DB.
  private final UserService userService;

  // Costruttore, Spring inserisce automaticamente UserService(Dependency Injection).
  public AuthController(UserService userService) {
    this.userService = userService;
  }

  // Metodo che risponde alle chiamate POST su "http://localhost:8080/api/auth/login".
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    // Chiamo il metodo login che controlla se l'email esiste e la password è la stessa
    User user = userService.login(request.getUsername(), request.getPassword());

    // Se UserService mi restituisce un utente, vuol dire che le credenziali sono giuste
    if (user != null) {
      // conversione  dell'Entity nel DTO sicuro per non avere password in rete
      UserResponse safeResponse = mapToResponse(user);
      
      // risposta con codice 200 e DTO senza password
      return ResponseEntity.ok(safeResponse);
    }

    // Se il UserService mi restituisce null, vuol dire che le credenziali sono sbagliate
    else {
      // Rispondo con codice 401  e un messaggio di errore
      return ResponseEntity.status(401).body("Email o password non validi");
    }
  }

  /**
   * Metodo per convertire un'Entity User nel DTO UserResponse.
   */
  private UserResponse mapToResponse(User user) {
      UserResponse dto = new UserResponse();
      dto.setId(user.getId());
      dto.setEmail(user.getEmail());
      dto.setName(user.getName());
      
      if (user.getRole() != null) {
          dto.setRole(user.getRole()); 
      }
      
      return dto;
  }
}