package com.bugboard.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.Enum.IssueStatus;
import com.bugboard.backend.model.Enum.IssueType;
import com.bugboard.backend.model.Enum.Priority;
import com.bugboard.backend.model.dto.IssueRequest;
import com.bugboard.backend.model.dto.IssueResponse;
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
  public List<IssueResponse> getAllIssues() {
    return issueService.getAllIssues();
  }

  // creazione issue con allegato
  @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  public ResponseEntity<IssueResponse> createIssue(
      @RequestPart("data") IssueRequest request,
      @RequestPart(value = "attachmentFile", required = false) MultipartFile attachmentFile) {
    try{
      IssueResponse createdIssue = issueService.createIssue(request, attachmentFile);
      return ResponseEntity.ok(createdIssue);
    }catch(RuntimeException e){
      // IssueService lancia RuntimeException sia per validazioni (titolo/descrizione/tipo mancanti)
      // sia per utente creatore non trovato: in entrambi i casi rispondiamo 400 invece di un 500.
      return ResponseEntity.badRequest().body(null);
    }
  }

  // assegnazione issue a un utente
  @PutMapping("/{id}/assign")
  public ResponseEntity<IssueResponse> assignIssue(
      @PathVariable Long id,
      @RequestParam Long assigneeId,
      @RequestParam Long userId // id dell'utente che fa l'assegnazione
  ) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Errore, Utente non trovato con ID " + userId));
    return ResponseEntity.ok(issueService.assignIssue(id, assigneeId, currentUser));
  }

  // aggiornamento per stato
  @PutMapping("/{id}/status")
  public ResponseEntity<IssueResponse> updateStatus(
    @PathVariable Long id,
    @RequestParam String status,
    @RequestParam Long userId
  ){
    try{
      IssueStatus newStatus = IssueStatus.valueOf(status.toUpperCase());
      IssueResponse updated = issueService.updateStatus(id,newStatus,userId);
      return ResponseEntity.ok(updated);
    }catch(IllegalArgumentException e){
      return ResponseEntity.badRequest().body(null);
    } catch(SecurityException e){
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }catch(RuntimeException e){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  // ENDPOINT PER I FILTRI

  // Endpoint per filtrare per STATO
  @GetMapping("/status/{status}")
  public List<IssueResponse> getIssuesByStatus(@PathVariable IssueStatus status) {
    // Passo la richiesta al Service che interrogherÃ  il DB
    return issueService.getIssuesByStatus(status);
  }

  // Endpoint per filtrare per TIPO
  @GetMapping("/type/{type}")
  public List<IssueResponse> getIssuesByType(@PathVariable IssueType type) {
    return issueService.getIssuesByType(type);
  }

  // Endpoint per filtrare per PRIORITÃ€
  @GetMapping("/priority/{priority}")
  public List<IssueResponse> getIssuesByPriority(@PathVariable Priority priority) {
    return issueService.getIssuesByPriority(priority);
  }

}
