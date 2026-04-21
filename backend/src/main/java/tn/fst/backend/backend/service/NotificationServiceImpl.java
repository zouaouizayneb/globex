package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Notification;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.repository.NotificationRepository;
import tn.fst.backend.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    @Override
    public Notification createNotification(Notification notification) {
        if (notification.getUser() != null) {
            userRepository.findById(notification.getUser().getIdUser())
                    .ifPresent(notification::setUser);
        }
        return notificationRepository.save(notification);
    }

    @Override
    public Notification updateNotification(Long id, Notification notificationDetails) {
        Optional<Notification> optional = notificationRepository.findById(id);
        if (!optional.isPresent()) throw new RuntimeException("Notification not found with id: " + id);

        Notification notif = optional.get();
        notif.setType(notificationDetails.getType());
        notif.setMessage(notificationDetails.getMessage());
        notif.setStatus(notificationDetails.getStatus());
        notif.setDateSend(notificationDetails.getDateSend());

        if (notificationDetails.getUser() != null)
            userRepository.findById(notificationDetails.getUser().getIdUser())
                    .ifPresent(notif::setUser);

        return notificationRepository.save(notif);
    }

    @Override
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) throw new RuntimeException("Notification not found with id: " + id);
        notificationRepository.deleteById(id);
    }
}
