package com.bugboard.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugboard.backend.model.Entity.Comment;
import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.dto.CommentRequest;
import com.bugboard.backend.model.dto.CommentResponse;
import com.bugboard.backend.model.dto.UserResponse;
import com.bugboard.backend.service.CommentService;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  // Endpoint per AGGIUNGERE un commento
  // Si chiamerà con una POST a: http://localhost:8080/api/comments
  @PostMapping
  public ResponseEntity<CommentResponse> addComment(@RequestBody CommentRequest request) {
    Comment nuovoCommento = commentService.addComment(request);
    // trasformazione entity in DTO per la risposta
    return ResponseEntity.ok(mapToResponse(nuovoCommento));
  }

  //  Endpoint per LEGGERE i commenti di una issue specifica
  // Si chiamerà con una GET a: http://localhost:8080/api/comments/issue/5
  @GetMapping("/issue/{issueId}")
  public ResponseEntity<List<CommentResponse>> getCommentsByIssue(@PathVariable Long issueId) {
    List<Comment> commenti = commentService.getCommentsByIssue(issueId);
    
    // map dell'intera lista di commenti in una DTO
    List<CommentResponse> responseList = commenti.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
        
    return ResponseEntity.ok(responseList);
  }

  /**
   * Metodo di utilità per convertire l'Entity Comment nel DTO CommentResponse.
   */
  private CommentResponse mapToResponse(Comment comment) {
      CommentResponse dto = new CommentResponse();
      dto.setId(comment.getId());
      dto.setText(comment.getText());
      dto.setCreationDate(comment.getCreationDate());
      
      // map dell'autore del commento senza una password
      if (comment.getAuthor() != null) {
          User author = comment.getAuthor();
          
          UserResponse authorDto = new UserResponse(
              author.getId(), 
              author.getEmail(), 
              author.getName(), 
              author.getRole()
          );
          
          dto.setAuthor(authorDto);
      }
      
      return dto;
  }
}
