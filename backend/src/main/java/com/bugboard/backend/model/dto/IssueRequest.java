package com.bugboard.backend.model.dto;
import com.bugboard.backend.model.Enum.IssueType;
import com.bugboard.backend.model.Enum.Priority;

//ho rinominato la classe con la maiuscola
public class IssueRequest {
    private String title;
    private String description;
    private IssueType issueType;
    private Priority issuePriority;

    //ho aggiunto questo campo per far sapere al Backend chi sta creando la issue.
    //il frontend ci manderà l'id dell'utente loggato.
    //uso Long (oggetto) e non long (primitivo) per evitare una NullPointerException
    //in fase di unboxing se il JSON in arrivo non contiene il campo o lo manda null.
    private Long creatorId;

    //aggiungere la parte delle immagini e/o allegati

    public IssueRequest() {}

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public IssueType getIssueType(){
        return issueType;
    }
    public void setIssueType(IssueType issueType){
        this.issueType = issueType;
    }

    public Priority getIssuePriority(){
        return issuePriority;
    }
    public void setIssuePriority(Priority issuePriority){
        this.issuePriority = issuePriority;
    }
    
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }


}
