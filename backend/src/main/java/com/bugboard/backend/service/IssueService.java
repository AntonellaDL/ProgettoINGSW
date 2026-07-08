package com.bugboard.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bugboard.backend.model.Entity.Issue;
import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.Enum.IssueStatus;
import com.bugboard.backend.model.Enum.IssueType;
import com.bugboard.backend.model.Enum.Priority;
import com.bugboard.backend.model.dto.IssueRequest;
import com.bugboard.backend.model.dto.IssueResponse;
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

  public List<IssueResponse> getAllIssues() {
    return issueRepository.findAll().stream().map(IssueResponse::new).collect(Collectors.toList());
  }

  public IssueResponse createIssue(IssueRequest request, MultipartFile attachmentFile) {
    //  Trova creatore
    if (request.getCreatorId() == null) {
      throw new RuntimeException("Errore, creatorId obbligatorio");
    }
    User creator = userRepository.findById(request.getCreatorId())
    .orElseThrow(() -> new RuntimeException("Errore, Utente non trovato con ID " + request.getCreatorId()));

        //valido i campi obbligatori
        if(request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Errore, il titolo è obbligatorio");
        }

        if(request.getDescription() == null || request.getDescription().isBlank()) {
            throw new RuntimeException("Errore, la descrizione è obbligatoria");
        }

        if(request.getIssueType() == null) {
            throw new RuntimeException("Errore, il tipo di issue è obbligatorio");
        }
//costruisco la nuova issue
    Issue nuovaIssue = new Issue();
    nuovaIssue.setTitle(request.getTitle());
    nuovaIssue.setDescription(request.getDescription());
    nuovaIssue.setType(request.getIssueType());
    nuovaIssue.setPriority(request.getIssuePriority());
    nuovaIssue.setCreator(creator);
    nuovaIssue.setStatus(IssueStatus.TODO);
    nuovaIssue.setCreatedAt(LocalDateTime.now());

    //  Gestione allegato
    if (attachmentFile != null && !attachmentFile.isEmpty()) {
      String fileName = fileStorageService.saveFile(attachmentFile);
      nuovaIssue.setAttachmentUrl("/uploads/" + fileName);
    }

    //  Salva Issue 
    Issue savedIssue = issueRepository.save(nuovaIssue);

    //  Salva nella History
    historyService.logCreationEvent(savedIssue, creator);

    return new IssueResponse(savedIssue);
  }

  // assegnazione 
  public IssueResponse assignIssue(Long issueId, Long assigneeId, User currentUser) {
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
    return new IssueResponse(savedIssue);
  }
  /*
  * Aggiornamento stato 
  * permette all'assegnatario di una segnalazione di modificare lo stato 
  * se il nuovo stato è DONE invia automaticamente una notifica al creatore 
  * issue ID è l'id della issue da aggiornare 
  * newStatus  nuovo stato richiesto 
  * ewquester id è l'id dell'utente che richiede l'aggiornamento 
  * RunTimeException se la issue o l'utente non esistono 
  * SecurityException se il richiedente non è l'assegnatario
  */
  public IssueResponse updateStatus(Long issueId, IssueStatus newStatus,Long requesterId){
    Issue issue = issueRepository.findById(issueId)
      .orElseThrow(()-> new RuntimeException("Issue non trovata con id "+issueId));
    //solo l'assegnatario può modificare lo stato
    if(issue.getAssignee()==null || !issue.getAssignee().getId().equals(requesterId)){
      throw new SecurityException("Solo l'utente assegnato può modificare lo stato della segnalazione.");
    }
    
    User requester = userRepository.findById(requesterId)
    . orElseThrow(()-> new RuntimeException("Utente non trovato con id "+ requesterId));

    IssueStatus oldStatus= issue.getStatus();
    issue.setStatus(newStatus);
    Issue saved= issueRepository.save(issue);

    //registra l'evento nello storico 
    historyService.logUpdateEvent(saved,requester,"lo stato",oldStatus.name(),newStatus.name());

    //quando la issue è risolta, notifica al creatore
    if(newStatus== IssueStatus.DONE && saved.getCreator()!=null){
      String message =String.format("La tua segnalazione \"%s\" è stata risolta da %s.", saved.getTitle(),requester.getName());
      notificationService.sendNotification(message,saved.getCreator());
    }

    return new IssueResponse(saved);
  }

  // SEZIONE FILTRI

  public List<IssueResponse> getIssuesByStatus(IssueStatus status) {
    return issueRepository.findByStatus(status).stream().map(IssueResponse::new).collect(Collectors.toList());
  }

  public List<IssueResponse> getIssuesByType(IssueType type) {
    return issueRepository.findByType(type).stream().map(IssueResponse::new).collect(Collectors.toList());
  }

  public List<IssueResponse> getIssuesByPriority(Priority priority) {
    return issueRepository.findByPriority(priority).stream().map(IssueResponse::new).collect(Collectors.toList());
  }
}