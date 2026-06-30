package com.bugboard.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bugboard.backend.model.Entity.Comment;

// non inserisco @Repository in quanto quando si utilizza Spring Data JPA 
// qualsiasi interfaccia che estende JpaRepository viene riconosciuta automaticamente 
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Questo metodo è per il requisito 5.
    // Quando apri un bug, vuoi vedere la lista dei suoi commenti.
    // Spring crea automaticamente la query SQL leggendo il nome del metodo:
    // "FindByIssueId (l'ID della issue collegata)".
    List<Comment> findByIssueId(Long issueId);

}
