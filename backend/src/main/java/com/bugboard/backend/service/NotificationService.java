package com.bugboard.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.bugboard.backend.model.Entity.Notification;
import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.repository.NotificationRepository;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;

  public NotificationService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  // Metodo per inviare una notifica
  public void sendNotification(String message, User recipient) {
    Notification notification = new Notification(message, recipient);
    notificationRepository.save(notification);
  }

  // Metodo per leggere le notifiche di un utente
  public List<Notification> getUserNotifications(Long userId) {
    return notificationRepository.findByRecipientIdOrderByDateDesc(userId);
  }

  // Metodo per segnare come letta
  public Notification markAsRead(Long notificationId) {
    return notificationRepository.findById(notificationId)
        .map(notification -> {
          notification.setRead(true);
          return notificationRepository.save(notification);
        })
        .orElseThrow(() -> new RuntimeException("Notifica non trovata id: " + notificationId));
  }
}
