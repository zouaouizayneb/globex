package tn.fst.backend.backend.controller;

import tn.fst.backend.backend.entity.Notification;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.repository.NotificationRepository;
import tn.fst.backend.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        return notification.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        if (notification.getUser() != null) {
            Optional<User> user = userRepository.findById(notification.getUser().getIdUser());
            if (user.isPresent()) {
                notification.setUser(user.get());
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }

        Notification savedNotification = notificationRepository.save(notification);
        return ResponseEntity.ok(savedNotification);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotification(@PathVariable Long id, @RequestBody Notification notificationDetails) {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (!optionalNotification.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Notification notification = optionalNotification.get();
        notification.setType(notificationDetails.getType());
        notification.setMessage(notificationDetails.getMessage());
        notification.setDateSend(notificationDetails.getDateSend());
        notification.setStatus(notificationDetails.getStatus());

        if (notificationDetails.getUser() != null) {
            Optional<User> user = userRepository.findById(notificationDetails.getUser().getIdUser());
            user.ifPresent(notification::setUser);
        }

        Notification updatedNotification = notificationRepository.save(notification);
        return ResponseEntity.ok(updatedNotification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        if (!notificationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        notificationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
