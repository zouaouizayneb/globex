package tn.fst.backend.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notif")
    private Long idNotif;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "date_send")
    private LocalDate dateSend = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public enum Status {
        READ,
        UNREAD
    }

    public Notification() {}

    // Getters & Setters
    public Long getIdNotif() { return idNotif; }
    public void setIdNotif(Long idNotif) { this.idNotif = idNotif; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDate getDateSend() { return dateSend; }
    public void setDateSend(LocalDate dateSend) { this.dateSend = dateSend; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

