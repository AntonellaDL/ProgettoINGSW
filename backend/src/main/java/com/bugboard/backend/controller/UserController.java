package com.bugboard.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.dto.UserRequest;
import com.bugboard.backend.model.dto.UserResponse; 
import com.bugboard.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest request) {
        try {
            //  conversione  DTO in entrata UserRequest in una vera Entity User
            User userToSave = new User();
            userToSave.setEmail(request.getEmail());
            userToSave.setPassword(request.getPassword()); 
            userToSave.setName(request.getName());
            userToSave.setRole(request.getRole());

            // passo la entity al service per salvarla nel database
            User createdUser = userService.createUser(userToSave);
            
            //map to DTO in uscita UserResponse
            UserResponse response = mapToResponse(createdUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        // entity recuperate nel db
        List<User> users = userService.getAllUsers();
        
        List<UserResponse> responseList = users.stream()
            .map(this::mapToResponse) 
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(responseList);
    }

    /**
     * Metodo per convertire un'Entity User nel DTO UserResponse.
     * In questo modo la password non viene mai inserita nell'oggetto che viaggia in rete.
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