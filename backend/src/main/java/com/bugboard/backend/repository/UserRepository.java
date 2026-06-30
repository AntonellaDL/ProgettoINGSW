package com.bugboard.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bugboard.backend.model.Entity.User;

//ho creato questa repository in modo da recuperare gli utenti dal database
//cosi da collegare un utente ad una issue
// non inserisco @Repository in quanto quando si utilizza Spring Data JPA 
// qualsiasi interfaccia che estende JpaRepository viene riconosciuta automaticamente
public interface UserRepository extends JpaRepository<User, Long> {

    // Trova un utente tramite email (utile per il login in futuro)
    Optional<User> findByEmail(String email);
    
}
