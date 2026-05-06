package com.bugboard.backend.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bugboard.backend.model.Entity.Issue;
import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.Enum.IssueStatus;
import com.bugboard.backend.model.Enum.IssueType;
import com.bugboard.backend.model.Enum.Priority;

import com.bugboard.backend.model.dto.IssueRequest;
import com.bugboard.backend.repository.UserRepository;
import com.bugboard.backend.service.IssueService;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

  private final IssueService issueService;
  private final UserRepository userRepository; // serve per il current user

  public IssueController(IssueService issueService, UserRepository userRepository) {
    this.issueService = issueService;
    this.userRepository = userRepository;
  }

  // Get tutte le issue
  @GetMapping
  public List<Issue> getAllIssues() {
    return issueService.getAllIssues();
  }

  // creazione issue con allegato
  @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  public ResponseEntity<Issue> createIssue(
      @RequestPart("data") IssueRequest request,
      @RequestPart(value = "attachmentFile", required = false) MultipartFile attachmentFile) {
    return ResponseEntity.ok(issueService.createIssue(request, attachmentFile));
  }

  // assegnazione issue a un utente
  @PutMapping("/{id}/assign")
  public ResponseEntity<Issue> assignIssue(
      @PathVariable Long id,
      @RequestParam Long assigneeId,
      @RequestParam Long userId // id dell'utente che fa l'assegnazione
  ) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Errore, Utente non trovato con ID " + userId));
    return ResponseEntity.ok(issueService.assignIssue(id, assigneeId, currentUser));
  }

  // ENDPOINT PER I FILTRI

  // Endpoint per filtrare per STATO
  @GetMapping("/status/{status}")
  public List<Issue> getIssuesByStatus(@PathVariable IssueStatus status) {
    // Passo la richiesta al Service che interrogherà il DB
    return issueService.getIssuesByStatus(status);
  }

  // Endpoint per filtrare per TIPO
  @GetMapping("/type/{type}")
  public List<Issue> getIssuesByType(@PathVariable IssueType type) {
    return issueService.getIssuesByType(type);
  }

  // Endpoint per filtrare per PRIORITÀ
  @GetMapping("/priority/{priority}")
  public List<Issue> getIssuesByPriority(@PathVariable Priority priority) {
    return issueService.getIssuesByPriority(priority);
  }

}
