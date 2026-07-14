package com.bugboard.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bugboard.backend.model.Entity.Issue;
import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.model.Enum.IssueStatus;
import com.bugboard.backend.model.Enum.IssueType;
import com.bugboard.backend.model.Enum.Priority;
import com.bugboard.backend.model.Enum.Role;
import com.bugboard.backend.model.dto.IssueRequest;
import com.bugboard.backend.model.dto.IssueResponse;
import com.bugboard.backend.repository.IssueRepository;
import com.bugboard.backend.repository.UserRepository;

/**
 * Test di Unità per il modulo IssueService.
 * I casi di test sono progettati applicando:
 * - Black-Box: Input Space Partitioning (N-WECT e R-WECT)
 * - White-Box: Branch Coverage
 */
@ExtendWith(MockitoExtension.class)
public class IssueServiceUnitTest {

    @Mock private IssueRepository issueRepository;
    @Mock private UserRepository userRepository;
    @Mock private HistoryService historyService;
    @Mock private NotificationService notificationService;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private IssueService issueService;

    private Issue mockIssue;
    private User mockAssignee;
    private User mockCurrentUser;

    @BeforeEach
    void setUp() {
        mockCurrentUser = new User("admin@bugboard.com", "pass", "Admin", Role.ADMIN);
        mockCurrentUser.setId(1L);

        mockAssignee = new User("dev@bugboard.com", "pass", "Sviluppatore", Role.USER);
        mockAssignee.setId(2L);

        mockIssue = new Issue();
        mockIssue.setId(100L);
        mockIssue.setTitle("Bug Critico");
        mockIssue.setStatus(IssueStatus.TODO);
    }

    // =================================================================================
    // TEST SUITE: assignIssue
    // =================================================================================

    // --- TEST N-WECT (Classi Valide) ---

    @Test
    @DisplayName("T1 (N-WECT): Validi e Assegnatario Diverso (Branch Coverage: True)")
    void assignIssue_ValidDifferentAssignee_ReturnsResponseAndTriggersSideEffects() {
        // Arrange
        Long issueId = 100L;
        Long assigneeId = 2L;
        
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(mockIssue));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(mockAssignee));
        when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        IssueResponse response = issueService.assignIssue(issueId, assigneeId, mockCurrentUser);

        // Assert
        assertNotNull(response);
        assertEquals("dev@bugboard.com", response.getAssigneeUsername());
        
        // Verifica White-Box: Assicura che il ramo if sia stato eseguito
        verify(historyService, times(1)).logUpdateEvent(any(), any(), any(), any(), any());
        verify(notificationService, times(1)).sendNotification(any(), any());
    }

    @Test
    @DisplayName("T2 (N-WECT): Validi e Assegnatario Uguale (Branch Coverage: False)")
    void assignIssue_ValidSameAssignee_ReturnsResponseNoSideEffects() {
        // Arrange
        Long issueId = 100L;
        Long assigneeId = 2L;
        mockIssue.setAssignee(mockAssignee); // la issue è già assegnata allo stesso sviluppatore
        
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(mockIssue));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(mockAssignee));
        when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        IssueResponse response = issueService.assignIssue(issueId, assigneeId, mockCurrentUser);

        // Assert
        assertNotNull(response);
        
        // Verifica White-Box: Assicura che i side-effect non siano stati chiamati
        verify(historyService, never()).logUpdateEvent(any(), any(), any(), any(), any());
        verify(notificationService, never()).sendNotification(any(), any());
    }

    // --- TEST R-WECT (Isolamento Classi Invalide) ---

    @Test
    @DisplayName("T3 (R-WECT): Issue Invalida (Branch Coverage: Exception 1)")
    void assignIssue_InvalidIssueId_ThrowsException() {
        // Arrange
        Long invalidIssueId = 999L;
        Long assigneeId = 2L;
        
        when(issueRepository.findById(invalidIssueId)).thenReturn(Optional.<Issue>empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.assignIssue(invalidIssueId, assigneeId, mockCurrentUser);
        });

        assertTrue(exception.getMessage().contains("Issue non trovata con ID 999"));
        // verifica white box, che l'eccezione sia lanciata prima di salvare l'issue
        verify(issueRepository, never()).save(any());
    }

    @Test
    @DisplayName("T4 (R-WECT): Assignee Invalido (Branch Coverage: Exception 2)")
    void assignIssue_InvalidAssigneeId_ThrowsException() {
        // Arrange
        Long issueId = 100L;
        Long invalidAssigneeId = 999L;
        
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(mockIssue));
        
        when(userRepository.findById(invalidAssigneeId)).thenReturn(Optional.<User>empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.assignIssue(issueId, invalidAssigneeId, mockCurrentUser);
        });

        assertTrue(exception.getMessage().contains("Utente non trovato con ID 999"));
    }

    // =================================================================================
    // TEST SUITE: createIssue
    // =================================================================================

    // --- TEST N-WECT (Classi Valide - Percorsi di Successo) ---

    @Test
    @DisplayName("T5 (N-WECT): Creazione con dati validi e SENZA allegato (Branch Allegato: False)")
    void createIssue_ValidDataNoAttachment_ReturnsResponse() {
        // Arrange
        IssueRequest request = new IssueRequest();
        request.setCreatorId(1L);
        request.setTitle("Bug Login");
        request.setDescription("Il login non funziona");
        request.setIssueType(IssueType.BUG);
        request.setIssuePriority(Priority.BASSA); // Valore di default

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockCurrentUser));
        when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        IssueResponse response = issueService.createIssue(request, null);

        // Assert
        assertNotNull(response);
        assertEquals("Bug Login", response.getTitle());
        // Verifica White-box
        verify(issueRepository, times(1)).save(any(Issue.class));
        verify(historyService, times(1)).logCreationEvent(any(Issue.class), eq(mockCurrentUser));
        verify(fileStorageService, never()).saveFile(any());
    }

    @Test
    @DisplayName("T6 (N-WECT): Creazione con dati validi e CON allegato (Branch Allegato: True)")
    void createIssue_ValidDataWithAttachment_ReturnsResponseAndSavesFile() {
        // Arrange
        IssueRequest request = new IssueRequest();
        request.setCreatorId(1L);
        request.setTitle("Bug Mappa");
        request.setDescription("Mappa non renderizza");
        request.setIssueType(IssueType.BUG);

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.saveFile(mockFile)).thenReturn("screenshot.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockCurrentUser));
        when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        IssueResponse response = issueService.createIssue(request, mockFile);

        // Assert
        assertNotNull(response);
        assertEquals("/uploads/screenshot.png", response.getAttachment());
        // Verifica White-box
        verify(fileStorageService, times(1)).saveFile(mockFile);
    }

    // --- TEST R-WECT (Isolamento Classi Invalide - Percorsi di Errore) ---

    @Test
    @DisplayName("T7 (R-WECT): ID Creatore Null (Branch: Exception CreatorId Null)")
    void createIssue_NullCreatorId_ThrowsException() {
        // Arrange
        IssueRequest request = new IssueRequest(); 
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.createIssue(request, null);
        });
        assertTrue(exception.getMessage().contains("creatorId obbligatorio"));
        verify(issueRepository, never()).save(any());
    }

    @Test
    @DisplayName("T8 (R-WECT): Creatore Inesistente (Branch: Exception User Not Found)")
    void createIssue_CreatorNotFound_ThrowsException() {
        // Arrange
        IssueRequest request = new IssueRequest();
        request.setCreatorId(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.createIssue(request, null);
        });
        assertTrue(exception.getMessage().contains("Utente non trovato"));
    }

    @Test
    @DisplayName("T9 (R-WECT): Titolo Blank (Branch: Exception Title Invalid)")
    void createIssue_BlankTitle_ThrowsException() {
        // Arrange
        IssueRequest request = new IssueRequest();
        request.setCreatorId(1L);
        request.setTitle("   "); 
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockCurrentUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.createIssue(request, null);
        });
        assertTrue(exception.getMessage().contains("titolo è obbligatorio"));
    }

    @Test
    @DisplayName("T10 (R-WECT): Descrizione Null (Branch: Exception Description Invalid)")
    void createIssue_NullDescription_ThrowsException() {
        // Arrange
        IssueRequest request = new IssueRequest();
        request.setCreatorId(1L);
        request.setTitle("Bug Valido");
        request.setDescription(null); 
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockCurrentUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.createIssue(request, null);
        });
        assertTrue(exception.getMessage().contains("descrizione è obbligatoria"));
    }

    @Test
    @DisplayName("T11 (R-WECT): Tipo Issue Null (Branch: Exception Type Invalid)")
    void createIssue_NullIssueType_ThrowsException() {
        // Arrange
        IssueRequest request = new IssueRequest();
        request.setCreatorId(1L);
        request.setTitle("Bug Valido");
        request.setDescription("Descrizione Valida");
        request.setIssueType(null); // Tipo mancante
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockCurrentUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.createIssue(request, null);
        });
        assertTrue(exception.getMessage().contains("tipo di issue è obbligatorio"));
    }

    @Test
    @DisplayName("T12 (Edge Case): File presente ma vuoto (Branch Allegato: Ignorato)")
    void createIssue_EmptyAttachment_DoesNotSaveFile() {
        // Arrange
        IssueRequest request = new IssueRequest();
        request.setCreatorId(1L);
        request.setTitle("Test File Vuoto");
        request.setDescription("Descrizione");
        request.setIssueType(IssueType.BUG);

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true); // file vuoto

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockCurrentUser));
        when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        issueService.createIssue(request, mockFile);

        // Assert: il file non deve essere salvato
        verify(fileStorageService, never()).saveFile(any());
    }


     // =================================================================================
    // TEST SUITE: updateStatus
    // =================================================================================
    @Test
    @DisplayName("T13: Cambio stato riuscito")
    void updateStatus_AuthorizedUser_Success() {
        Long issueId = 100L;
        Long requesterId = 2L;
        mockIssue.setAssignee(mockAssignee);
        
        when(issueRepository.findById(issueId)).thenReturn(Optional.<Issue>of(mockIssue));
        when(userRepository.findById(requesterId)).thenReturn(Optional.<User>of(mockAssignee));
        when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

        IssueResponse response = issueService.updateStatus(issueId, IssueStatus.IN_PROGRESS, requesterId);

        assertEquals(IssueStatus.IN_PROGRESS, response.getStatus());
        verify(historyService, times(1)).logUpdateEvent(any(), any(), eq("lo stato"), any(), any());
    }

    @Test
    @DisplayName("T14: SecurityException se l'utente non è l'assegnatario")
    void updateStatus_UnauthorizedUser_ThrowsSecurityException() {
        Long issueId = 100L;
        mockIssue.setAssignee(mockAssignee); // Assegnato a ID 2
        
        when(issueRepository.findById(issueId)).thenReturn(Optional.<Issue>of(mockIssue));

        assertThrows(SecurityException.class, () -> {
            issueService.updateStatus(issueId, IssueStatus.IN_PROGRESS, 1L);
        });
    }

    @Test
    @DisplayName("T15: Notifica inviata se lo stato diventa DONE")
    void updateStatus_ToDone_TriggersNotification() {
        Long issueId = 100L;
        Long requesterId = 2L;
        mockIssue.setAssignee(mockAssignee);
        mockIssue.setCreator(mockCurrentUser);
        
        when(issueRepository.findById(issueId)).thenReturn(Optional.<Issue>of(mockIssue));
        when(userRepository.findById(requesterId)).thenReturn(Optional.<User>of(mockAssignee));
        when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

        issueService.updateStatus(issueId, IssueStatus.DONE, requesterId);

        verify(notificationService, times(1)).sendNotification(contains("risolta"), eq(mockCurrentUser));
    }

         @Test
    @DisplayName("T16: RuntimeException se l'Issue non viene trovata")
    void updateStatus_IssueNotFound_ThrowsException() {
        Long issueId = 999L;
        when(issueRepository.findById(issueId)).thenReturn(Optional.<Issue>empty());

       RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            issueService.updateStatus(issueId, IssueStatus.IN_PROGRESS, 1L);
        });
        assertTrue(ex.getMessage().contains("Issue non trovata"));
    }

  @Test
@DisplayName("T17: RuntimeException se il requester non viene trovato")
void updateStatus_RequesterNotFound_ThrowsException() {

    Long issueId = 100L;
    Long requesterId = 999L;

    User fakeAssignee = new User("fake@test.it", "pass", "Fake", Role.USER);
    fakeAssignee.setId(requesterId);

    mockIssue.setAssignee(fakeAssignee);

    when(issueRepository.findById(issueId)).thenReturn(Optional.of(mockIssue));
    when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

      RuntimeException ex = assertThrows(RuntimeException.class, () ->
          issueService.updateStatus(issueId, IssueStatus.IN_PROGRESS, requesterId)
      );
      assertTrue(ex.getMessage().contains("Utente non trovato"));
}

    @Test
    @DisplayName("T18: SecurityException se l'assegnatario è null")
    void updateStatus_NullAssignee_ThrowsSecurityException() {
        // Arrange
        Long issueId = 100L;
        mockIssue.setAssignee(null); // Assegnatario mancante
        
        when(issueRepository.findById(issueId)).thenReturn(Optional.<Issue>of(mockIssue));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            issueService.updateStatus(issueId, IssueStatus.IN_PROGRESS, 2L);
        });
    }

}