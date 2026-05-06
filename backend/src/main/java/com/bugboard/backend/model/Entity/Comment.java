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
@Table(name = "comments")
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Ho messo il limite a 1000 caratteri e l'obbligo di inserimento (nullable =
  // false)
  // per evitare commenti vuoti nel database.
  @Column(nullable = false, length = 1000)
  private String text;

  private LocalDateTime creationDate;

  // RELAZIONI

  // 1. Relazione con l'Utente (AutoreCommento)
  // Ho usato @ManyToOne perché un singolo utente può scrivere N commenti.
  // nullable = false perché un commento deve per forza avere un autore.
  @ManyToOne
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  // 2. Relazione con la Issue
  // @ManyToOne: tanti commenti appartengono a una singola Issue.
  @ManyToOne
  @JoinColumn(name = "issue_id", nullable = false)
  private Issue issue;

  // Costruttore vuoto
  public Comment() {
  }

  // --- Getter e Setter ---

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

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public Issue getIssue() {
    return issue;
  }

  public void setIssue(Issue issue) {
    this.issue = issue;
  }
}
