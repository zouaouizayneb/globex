package tn.fst.backend.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_messages")
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_msg")
    private Long idMsg;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(name = "date_msg")
    private LocalDateTime dateMsg = LocalDateTime.now();

    // Relation avec User
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public ChatbotMessage() {}

    // Getters & Setters
    public Long getIdMsg() { return idMsg; }
    public void setIdMsg(Long idMsg) { this.idMsg = idMsg; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public LocalDateTime getDateMsg() { return dateMsg; }
    public void setDateMsg(LocalDateTime dateMsg) { this.dateMsg = dateMsg; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}


