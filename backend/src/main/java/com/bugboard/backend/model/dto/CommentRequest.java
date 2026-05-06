package com.bugboard.backend.model.dto;

public class CommentRequest {

    // Il testo del messaggio
    private String text;

    // L'ID della Issue dove stiamo commentando
    private Long issueId;

    // L'ID dell'utente che sta scrivendo (chi è loggato)
    private Long authorId;

    public CommentRequest() {
    }

    // --- Getter e Setter ---
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
}
