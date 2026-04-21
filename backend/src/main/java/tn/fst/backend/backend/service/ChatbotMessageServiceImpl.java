package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.ChatbotMessage;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.repository.ChatbotRepository;
import tn.fst.backend.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatbotMessageServiceImpl implements ChatbotMessageService {

    @Autowired
    private ChatbotRepository chatbotMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<ChatbotMessage> getAllMessages() {
        return chatbotMessageRepository.findAll();
    }

    @Override
    public Optional<ChatbotMessage> getMessageById(Long id) {
        return chatbotMessageRepository.findById(id);
    }

    @Override
    public ChatbotMessage createMessage(ChatbotMessage message) {
        if (message.getUser() != null) {
            userRepository.findById(message.getUser().getIdUser())
                    .ifPresent(message::setUser);
        }
        return chatbotMessageRepository.save(message);
    }

    @Override
    public ChatbotMessage updateMessage(Long id, ChatbotMessage messageDetails) {
        Optional<ChatbotMessage> optional = chatbotMessageRepository.findById(id);
        if (!optional.isPresent()) throw new RuntimeException("Message not found with id: " + id);

        ChatbotMessage msg = optional.get();
        msg.setQuestion(messageDetails.getQuestion());
        msg.setAnswer(messageDetails.getAnswer());
        msg.setDateMsg(messageDetails.getDateMsg());

        if (messageDetails.getUser() != null)
            userRepository.findById(messageDetails.getUser().getIdUser())
                    .ifPresent(msg::setUser);

        return chatbotMessageRepository.save(msg);
    }

    @Override
    public void deleteMessage(Long id) {
        if (!chatbotMessageRepository.existsById(id)) throw new RuntimeException("Message not found with id: " + id);
        chatbotMessageRepository.deleteById(id);
    }
}

