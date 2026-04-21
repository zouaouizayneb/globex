package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.ChatbotMessage;
import java.util.List;
import java.util.Optional;

public interface ChatbotMessageService {
    List<ChatbotMessage> getAllMessages();
    Optional<ChatbotMessage> getMessageById(Long id);
    ChatbotMessage createMessage(ChatbotMessage message);
    ChatbotMessage updateMessage(Long id, ChatbotMessage message);
    void deleteMessage(Long id);
}
