package com.bugboard.backend.model.dto;

import java.time.LocalDateTime;

public class CommentResponse {
    
    private Long id;
    private String text;
    private LocalDateTime creationDate;
    
  //la password viene filtrata prima di essere inviata
    private UserResponse author; 

    public CommentResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public UserResponse getAuthor() {
        return author;
    }

    public void setAuthor(UserResponse author) {
        this.author = author;
    }
}