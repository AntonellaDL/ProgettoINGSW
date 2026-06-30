package com.bugboard.backend.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.Enum.Role;
import com.bugboard.backend.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    //creazione utente usato dall'admin
    public User createUser (User user) {
        return userRepository.save(user);
    }

    //recupera tutti gli utenti
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //Login utente
    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                return user; // Login riuscito
            }
        }
        return null; // Login fallito
    }

    //admin di default
    @PostConstruct
    public void initAdminUser() {
        //controllo se il database è vuoto
        if (userRepository.count() == 0) {
            // //se non esiste creo un admin di default
            User admin = new User( "admin@bugboard.com","admin123", "Admin", Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin di default creato: " + admin.getEmail());
        }
    }
}