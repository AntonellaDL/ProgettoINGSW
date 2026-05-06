package com.bugboard.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugboard.backend.model.Entity.Comment;
import com.bugboard.backend.model.dto.CommentRequest;
import com.bugboard.backend.service.CommentService;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  // 1. Endpoint per AGGIUNGERE un commento
  // Si chiamerà con una POST a: http://localhost:8080/api/comments
  @PostMapping
  public ResponseEntity<Comment> addComment(@RequestBody CommentRequest request) {
    Comment nuovoCommento = commentService.addComment(request);
    return ResponseEntity.ok(nuovoCommento);
  }

  // 2. Endpoint per LEGGERE i commenti di una issue specifica
  // Si chiamerà con una GET a: http://localhost:8080/api/comments/issue/5
  @GetMapping("/issue/{issueId}")
  public ResponseEntity<List<Comment>> getCommentsByIssue(@PathVariable Long issueId) {
    List<Comment> commenti = commentService.getCommentsByIssue(issueId);
    return ResponseEntity.ok(commenti);
  }
}
