package tn.fst.backend.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.ChatConversation;
import tn.fst.backend.backend.entity.ChatbotMessage;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.repository.ChatConversationRepository;
import tn.fst.backend.backend.repository.ChatbotMessageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatConversationRepository conversationRepository;

    @Autowired
    private ChatbotMessageRepository messageRepository;

    @Override
    public List<ChatConversationResponse> getUserConversations(User user) {
        List<ChatConversation> conversations = conversationRepository.findByUserOrderByUpdatedAtDesc(user);
        return conversations.stream()
                .map(this::mapToConversationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChatConversationResponse getConversationById(Long id, User user) {
        ChatConversation conversation = conversationRepository.findByIdConversationAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return mapToConversationResponse(conversation);
    }

    @Override
    @Transactional
    public ChatConversationResponse createConversation(CreateConversationRequest request, User user) {
        ChatConversation conversation = new ChatConversation(request.getTitle(), user);
        ChatConversation saved = conversationRepository.save(conversation);
        return mapToConversationResponse(saved);
    }

    @Override
    @Transactional
    public ChatConversationResponse updateConversationTitle(Long id, String title, User user) {
        ChatConversation conversation = conversationRepository.findByIdConversationAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setTitle(title);
        ChatConversation saved = conversationRepository.save(conversation);
        return mapToConversationResponse(saved);
    }

    @Override
    @Transactional
    public void deleteConversation(Long id, User user) {
        ChatConversation conversation = conversationRepository.findByIdConversationAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversationRepository.delete(conversation);
    }

    @Override
    public List<ChatMessageResponse> getConversationMessages(Long conversationId, User user) {
        ChatConversation conversation = conversationRepository.findByIdConversationAndUser(conversationId, user)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        List<ChatbotMessage> messages = messageRepository.findByConversationOrderByDateMsgAsc(conversation);
        return messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long conversationId, SendMessageRequest request, User user) {
        ChatConversation conversation = conversationRepository.findByIdConversationAndUser(conversationId, user)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        ChatbotMessage message = new ChatbotMessage();
        message.setQuestion(request.getQuestion());
        message.setConversation(conversation);
        
        // TODO: Integrate with AI/chatbot service to generate answer
        // For now, using a simple placeholder response
        message.setAnswer(generatePlaceholderAnswer(request.getQuestion()));
        
        ChatbotMessage saved = messageRepository.save(message);
        return mapToMessageResponse(saved);
    }

    @Override
    @Transactional
    public ChatConversationResponse createConversationWithMessage(CreateConversationRequest request, SendMessageRequest messageRequest, User user) {
        ChatConversation conversation = new ChatConversation(request.getTitle(), user);
        ChatConversation savedConversation = conversationRepository.save(conversation);

        ChatbotMessage message = new ChatbotMessage();
        message.setQuestion(messageRequest.getQuestion());
        message.setConversation(savedConversation);
        
        // TODO: Integrate with AI/chatbot service to generate answer
        message.setAnswer(generatePlaceholderAnswer(messageRequest.getQuestion()));
        
        messageRepository.save(message);

        return mapToConversationResponse(savedConversation);
    }

    private ChatConversationResponse mapToConversationResponse(ChatConversation conversation) {
        ChatConversationResponse response = new ChatConversationResponse();
        response.setIdConversation(conversation.getIdConversation());
        response.setTitle(conversation.getTitle());
        response.setUserId(conversation.getUser().getIdUser());
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());
        response.setMessageCount(conversation.getMessages().size());
        
        List<ChatMessageResponse> messages = conversation.getMessages().stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
        response.setMessages(messages);
        
        return response;
    }

    private ChatMessageResponse mapToMessageResponse(ChatbotMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setIdMsg(message.getIdMsg());
        response.setQuestion(message.getQuestion());
        response.setAnswer(message.getAnswer());
        response.setDateMsg(message.getDateMsg());
        response.setConversationId(message.getConversation().getIdConversation());
        return response;
    }

    private String generatePlaceholderAnswer(String question) {
        // Placeholder for AI/chatbot integration
        // This should be replaced with actual AI service call
        return "Thank you for your question: \"" + question + "\". Our team will get back to you shortly.";
    }
}
