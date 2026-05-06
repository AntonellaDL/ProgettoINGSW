package com.bugboard.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bugboard.backend.model.Entity.Comment;
import com.bugboard.backend.model.Entity.Issue;
import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.dto.CommentRequest;
import com.bugboard.backend.repository.CommentRepository;
import com.bugboard.backend.repository.IssueRepository;
import com.bugboard.backend.repository.UserRepository;

@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final IssueRepository issueRepository;
  private final UserRepository userRepository;

  // tutti i repository necessari:
  // - commentRepository: per salvare il commento
  // - issueRepository: per verificare che la issue esista
  // - userRepository: per verificare che l'autore esista
  public CommentService(CommentRepository commentRepository,
      IssueRepository issueRepository,
      UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.issueRepository = issueRepository;
    this.userRepository = userRepository;
  }

  // Metodo per aggiungere un commento
  public Comment addComment(CommentRequest request) {

    // 1. Recupero la Issue dal DB. Se non esiste, lancio un errore.
    Issue issue = issueRepository.findById(request.getIssueId())
        .orElseThrow(() -> new RuntimeException("Issue non trovata con ID: " + request.getIssueId()));

    // 2. Recupero l'Utente (AutoreCommento) dal DB.
    User author = userRepository.findById(request.getAuthorId())
        .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + request.getAuthorId()));

    // 3. Creo il nuovo oggetto Commento
    Comment newComment = new Comment();
    newComment.setText(request.getText());
    newComment.setCreationDate(LocalDateTime.now());

    // 4. Collego le relazioni (Il commento punta a User e Issue)
    newComment.setIssue(issue);
    newComment.setAuthor(author);

    // 5. Salvataggio nel database
    return commentRepository.save(newComment);
  }

  // Metodo per leggere tutti i commenti di una specifica issue
  // Questo serve una volta aperta la pagina di dettaglio del bug
  public List<Comment> getCommentsByIssue(Long issueId) {
    return commentRepository.findByIssueId(issueId);
  }
}
