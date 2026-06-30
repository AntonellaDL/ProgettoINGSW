package com.bugboard.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bugboard.backend.model.Entity.Notification;

// non inserisco @Repository in quanto quando si utilizza Spring Data JPA 
// qualsiasi interfaccia che estende JpaRepository viene riconosciuta automaticamente
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // Trova tutte le notifiche di un utente
  List<Notification> findByRecipientIdOrderByDateDesc(Long recipientId);

  // Trova solo le notifiche non lette di un utente (può essere utile per il
  // contatore delle notifiche)
  List<Notification> findByRecipientIdAndIsReadFalse(Long recipientId);
}
