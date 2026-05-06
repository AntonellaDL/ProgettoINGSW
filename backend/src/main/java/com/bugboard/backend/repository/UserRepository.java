package com.bugboard.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bugboard.backend.model.Entity.User;

//ho creato questa repository in modo da recuperare gli utenti dal database
//cosi da collegare un utente ad una issue
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Trova un utente tramite email (utile per il login in futuro)
    Optional<User> findByEmail(String email);
    
}
