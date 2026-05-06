package com.bugboard.backend.model.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "history_log")
public class History {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Descrizione dell'azione (Creazione Issue, Stato cambiato in todo)
  @Column(nullable = false)
  private String action;

  // Quando è successo
  private LocalDateTime date;

  // Chi ha fatto l'azione
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // Su quale issue è stata fatta l'azione
  @ManyToOne
  @JoinColumn(name = "issue_id", nullable = false)
  private Issue issue;

  public History() {
  }

  // --- Getter e Setter ---

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

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Issue getIssue() {
    return issue;
  }

  public void setIssue(Issue issue) {
    this.issue = issue;
  }
}
