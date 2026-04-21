package tn.fst.backend.backend.controller;

import tn.fst.backend.backend.entity.ChatbotMessage;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.repository.ChatbotRepository;
import tn.fst.backend.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatbot-messages")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatbotController {

    @Autowired
    private ChatbotRepository chatbotMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<ChatbotMessage> getAllMessages() {
        return chatbotMessageRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatbotMessage> getMessageById(@PathVariable Long id) {
        Optional<ChatbotMessage> message = chatbotMessageRepository.findById(id);
        return message.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ChatbotMessage> createMessage(@RequestBody ChatbotMessage message) {
        // Vérifier si l'utilisateur existe
        if (message.getUser() != null) {
            Optional<User> user = userRepository.findById(message.getUser().getIdUser());
            if (user.isPresent()) {
                message.setUser(user.get());
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }

        ChatbotMessage savedMessage = chatbotMessageRepository.save(message);
        return ResponseEntity.ok(savedMessage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChatbotMessage> updateMessage(@PathVariable Long id, @RequestBody ChatbotMessage messageDetails) {
        Optional<ChatbotMessage> optionalMessage = chatbotMessageRepository.findById(id);
        if (!optionalMessage.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ChatbotMessage message = optionalMessage.get();
        message.setQuestion(messageDetails.getQuestion());
        message.setAnswer(messageDetails.getAnswer());
        message.setDateMsg(messageDetails.getDateMsg());

        // Mettre à jour l'utilisateur si fourni
        if (messageDetails.getUser() != null) {
            Optional<User> user = userRepository.findById(messageDetails.getUser().getIdUser());
            user.ifPresent(message::setUser);
        }

        ChatbotMessage updatedMessage = chatbotMessageRepository.save(message);
        return ResponseEntity.ok(updatedMessage);
    }

    // Supprimer un message
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        if (!chatbotMessageRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        chatbotMessageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
