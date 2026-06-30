package com.bugboard.backend.model.dto;

import java.time.LocalDateTime;

import com.bugboard.backend.model.Entity.Issue;
import com.bugboard.backend.model.Enum.IssueStatus;
import com.bugboard.backend.model.Enum.IssueType;
import com.bugboard.backend.model.Enum.Priority;


/*DTO per le risposte delle issue in quanto
*  l'entità Issue contiene oggetti User (creator, assignee) che a loro
 * volta hanno il campo "password". Restituire Issue direttamente esporrebbe
* dati sensibili nel JSON. Questo DTO appiattisce solo i campi necessari.
*/


public class IssueResponse {

    private Long id;
    private String title;
    private String description;
    private IssueStatus status;
    private Priority priority;
    private IssueType type;
    private String attachment;
    private LocalDateTime createdAt;

    //dati del creatore 
    private Long creatorId;
    private String creatorUsername;

    //dati dell'assegnatario
    private Long assigneeId;
    private String assigneeUsername;

    //costruttore vuoto
    public IssueResponse() {}

    //costruttore che prende un oggetto Issue e lo trasforma in un IssueResponse
    public IssueResponse(Issue issue) {
        
        this.id = issue.getId();
        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.status = issue.getStatus();
        this.priority = issue.getPriority();
        this.type = issue.getType();
        this.attachment = issue.getAttachmentUrl();
        this.createdAt = issue.getCreatedAt();

        if(issue.getCreator() != null) {
            this.creatorId = issue.getCreator().getId();
            this.creatorUsername = issue.getCreator().getName();
        }
        if(issue.getAssignee() != null) {
            this.assigneeId = issue.getAssignee().getId();
            this.assigneeUsername = issue.getAssignee().getName();
        }
    }

    //solo getter in quanto questo DTO è solo 
    // per la lettura, non per la scrittura

    public Long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public IssueStatus getStatus() {
        return status;
    }
    public Priority getPriority() {
        return priority;
    }
    public IssueType getType() {
        return type;
    }
    public String getAttachment() {
        return attachment;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public Long getCreatorId() {
        return creatorId;
    }
    public String getCreatorUsername() {
        return creatorUsername;
    }
    public Long getAssigneeId() {
        return assigneeId;
    }
    public String getAssigneeUsername() {
        return assigneeUsername;
    }
}
