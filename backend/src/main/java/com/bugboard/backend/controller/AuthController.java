package com.bugboard.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.dto.LoginRequest;
import com.bugboard.backend.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

  // Tramite UserService controllo le password nel DB.
  private final UserService userService;

  // Costruttore, Spring inserisce automaticamente UserService(Dependency
  // Injection).
  public AuthController(UserService userService) {
    this.userService = userService;
  }

  // Metodo che risponde alle chiamate POST su
  // "http://localhost:8080/api/auth/login".
  // Uso POST perché stiamo inviando password, che non devono vedersi nell'URL
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    // @RequestBody prende il JSON inviato dal Frontend e lo trasforma nell'oggetto
    // Java LoginRequest.
    // LoginRequest contiene email e password

    // Chiamo il metodo login che controlla se l'email esiste e la password è la
    // stessa
    User user = userService.login(request.getUsername(), request.getPassword());

    // Se UserService mi restituisce un utente, vuol dire che le credenziali sono
    // giuste
    if (user != null) {
      // Rispondo con codice 200 (OK) e restituisco i dati dell'utente
      // Il Frontend userà questi dati per sapere chi si è loggato
      return ResponseEntity.ok(user);
    }

    // Se il UserService mi restituisce null, vuol dire che le credenziali sono
    // sbagliate
    else {
      // Rispondo con codice 401 (Unauthorized) e un messaggio di errore
      return ResponseEntity.status(401).body("Email o password non validi");
    }
  }
}
