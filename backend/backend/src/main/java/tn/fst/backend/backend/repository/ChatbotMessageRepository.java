package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.ChatConversation;
import tn.fst.backend.backend.entity.ChatbotMessage;

import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    List<ChatbotMessage> findByConversationOrderByDateMsgAsc(ChatConversation conversation);

    @Query("SELECT m FROM ChatbotMessage m WHERE m.conversation = :conversation ORDER BY m.dateMsg ASC")
    List<ChatbotMessage> findMessagesByConversation(@Param("conversation") ChatConversation conversation);

    void deleteByConversation(ChatConversation conversation);
}
