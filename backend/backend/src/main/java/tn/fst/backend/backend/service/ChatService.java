package tn.fst.backend.backend.service;

import tn.fst.backend.backend.dto.ChatConversationResponse;
import tn.fst.backend.backend.dto.ChatMessageResponse;
import tn.fst.backend.backend.dto.CreateConversationRequest;
import tn.fst.backend.backend.dto.SendMessageRequest;
import tn.fst.backend.backend.entity.ChatConversation;
import tn.fst.backend.backend.entity.User;

import java.util.List;

public interface ChatService {

    List<ChatConversationResponse> getUserConversations(User user);

    ChatConversationResponse getConversationById(Long id, User user);

    ChatConversationResponse createConversation(CreateConversationRequest request, User user);

    ChatConversationResponse updateConversationTitle(Long id, String title, User user);

    void deleteConversation(Long id, User user);

    List<ChatMessageResponse> getConversationMessages(Long conversationId, User user);

    ChatMessageResponse sendMessage(Long conversationId, SendMessageRequest request, User user);

    ChatConversationResponse createConversationWithMessage(CreateConversationRequest request, SendMessageRequest messageRequest, User user);
}
