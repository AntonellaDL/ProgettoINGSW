package com.bugboard.frontend.model;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Comment {
    private Long id;
    private String text;
    private String author;

    @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss" )
    private LocalDateTime creationDate;

    public Comment(){}

    public Comment(Long id, String text, String author, LocalDateTime creationDate){
        this.id = id;
        this.text = text;
        this.author = author;
        this.creationDate = creationDate;
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
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
   // Questo metodo intercetta l'oggetto JSON e ne estrae solo il nome
   @JsonProperty("author")
    public void setAuthorFromJson(Map<String, Object> authorData) {
        if (authorData != null) {
            this.author = (String) authorData.get("email");
        }
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    //per uso normale, quando si aggiunge un commento
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    //per uso con JSON, quando si ricevono commenti dal backend
    @JsonProperty("creationDate")
    public void setCreationDateFromJson(String dateString) {
        if (dateString != null) {
            // Convertiamo noi la stringa in un oggetto LocalDateTime
            this.creationDate = java.time.LocalDateTime.parse(dateString);
        }
    }

    @Override
    public String toString() {
        return author + ":" + text +"(" + creationDate + ")";
    }
    
}
