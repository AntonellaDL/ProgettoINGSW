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
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Il messaggio della notifica
  @Column(nullable = false)
  private String message;

  // Quando è stata creata
  private LocalDateTime date;

  // Se l'utente l'ha già letta
  private boolean isRead = false;

  // A chi è destinata la notifica
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User recipient;

  public Notification() {
  }

  // Costruttore
  public Notification(String message, User recipient) {
    this.message = message;
    this.recipient = recipient;
    this.date = LocalDateTime.now();
    this.isRead = false;
  }

  // Getter e Setter
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean isRead) {
    this.isRead = isRead;
  }

  public User getRecipient() {
    return recipient;
  }

  public void setRecipient(User recipient) {
    this.recipient = recipient;
  }
}
