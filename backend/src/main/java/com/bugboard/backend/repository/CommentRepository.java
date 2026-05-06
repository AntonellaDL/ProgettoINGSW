package com.bugboard.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bugboard.backend.model.Entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Questo metodo è per il requisito 5.
    // Quando apri un bug, vuoi vedere la lista dei suoi commenti.
    // Spring crea automaticamente la query SQL leggendo il nome del metodo:
    // "FindByIssueId (l'ID della issue collegata)".
    List<Comment> findByIssueId(Long issueId);

}
