package com.bugboard.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bugboard.backend.model.Entity.Issue;
import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.Enum.IssueStatus;
import com.bugboard.backend.model.Enum.IssueType;
import com.bugboard.backend.model.Enum.Priority;
import com.bugboard.backend.model.dto.IssueRequest;
import com.bugboard.backend.repository.IssueRepository;
import com.bugboard.backend.repository.UserRepository;

@Service
public class IssueService {

  private final IssueRepository issueRepository;
  private final UserRepository userRepository;
  private final HistoryService historyService;
  private final FileStorageService fileStorageService;
  private final NotificationService notificationService;

  public IssueService(IssueRepository issueRepository, UserRepository userRepository,
      HistoryService historyService, FileStorageService fileStorageService,
      NotificationService notificationService) {
    this.issueRepository = issueRepository;
    this.userRepository = userRepository;
    this.historyService = historyService;
    this.fileStorageService = fileStorageService;
    this.notificationService = notificationService;
  }

  // metodi di base

  public List<Issue> getAllIssues() {
    return issueRepository.findAll();
  }

  public Issue createIssue(IssueRequest request, MultipartFile attachmentFile) {
    // 1. Trova creatore
    User creator = userRepository.findById(request.getCreatorId())
        .orElseThrow(() -> new RuntimeException("Errore, Utente non trovato con ID " + request.getCreatorId()));

    Issue nuovaIssue = new Issue();
    nuovaIssue.setTitle(request.getTitle());
    nuovaIssue.setDescription(request.getDescription());
    nuovaIssue.setType(request.getIssueType());
    nuovaIssue.setPriority(request.getIssuePriority());
    nuovaIssue.setCreator(creator);
    nuovaIssue.setStatus(IssueStatus.TODO);
    nuovaIssue.setCreatedAt(LocalDateTime.now());

    // 2. Gestione allegato
    if (attachmentFile != null && !attachmentFile.isEmpty()) {
      String fileName = fileStorageService.saveFile(attachmentFile);
      nuovaIssue.setAttachmentUrl("/uploads/" + fileName);
    }

    // 3. Salva Issue (era dentro parentesi sbagliate)
    Issue savedIssue = issueRepository.save(nuovaIssue);

    // 4. Salva nella History
    historyService.logCreationEvent(savedIssue, creator);

    return savedIssue;
  }

  public Issue assignIssue(Long issueId, Long assigneeId, User currentUser) {
    Issue issue = issueRepository.findById(issueId)
        .orElseThrow(() -> new RuntimeException("Errore, Issue non trovata con ID " + issueId));

    User newAssignee = userRepository.findById(assigneeId)
        .orElseThrow(() -> new RuntimeException("Errore, Utente non trovato con ID " + assigneeId));

    String oldAssigneeName = issue.getAssignee() != null ? issue.getAssignee().getName() : "Nessuno";

    issue.setAssignee(newAssignee);
    Issue savedIssue = issueRepository.save(issue);

    if (!oldAssigneeName.equals(newAssignee.getName())) {
      // Loggo nella history
      historyService.logUpdateEvent(savedIssue, currentUser, "l'assegnatario", oldAssigneeName, newAssignee.getName());

      // Invio la notifica
      notificationService.sendNotification(
          "Ti è stato assegnato il bug: " + savedIssue.getTitle(),
          newAssignee);
    }
    return savedIssue;
  }

  // SEZIONE FILTRI

  public List<Issue> getIssuesByStatus(IssueStatus status) {
    return issueRepository.findByStatus(status);
  }

  public List<Issue> getIssuesByType(IssueType type) {
    return issueRepository.findByType(type);
  }

  public List<Issue> getIssuesByPriority(Priority priority) {
    return issueRepository.findByPriority(priority);
  }
}
