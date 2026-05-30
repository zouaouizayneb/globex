package tn.fst.backend.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatConversationResponse {

    private Long idConversation;
    private String title;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChatMessageResponse> messages;
    private Integer messageCount;

    public ChatConversationResponse() {}

    public ChatConversationResponse(Long idConversation, String title, Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.idConversation = idConversation;
        this.title = title;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getIdConversation() {
        return idConversation;
    }

    public void setIdConversation(Long idConversation) {
        this.idConversation = idConversation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ChatMessageResponse> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageResponse> messages) {
        this.messages = messages;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
}
