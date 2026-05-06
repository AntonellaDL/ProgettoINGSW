package com.bugboard.frontend.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class HistoryLog {
    private Long id;
    private String action;
    private String author;
    private String details;

    @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss" )
    private LocalDateTime timeStamp;
    public HistoryLog(){}

    public HistoryLog(Long id, String action, String author, String details, LocalDateTime timeStamp){
        this.id = id;
        this.action = action;
        this.author = author;
        this.details = details;
        this.timeStamp = timeStamp;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return timeStamp + " :" + author + "-" + action;
    }

}
