package com.bugboard.backend.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bugboard.backend.model.Entity.Notification;
import com.bugboard.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

  // Uso il Service
  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping("/user/{userId}")
  public List<Notification> getUserNotifications(@PathVariable Long userId) {
    return notificationService.getUserNotifications(userId);
  }

  @PutMapping("/{id}/read")
  public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(notificationService.markAsRead(id));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
