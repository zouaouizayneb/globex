package tn.fst.backend.backend.repository;

import tn.fst.backend.backend.entity.ChatbotMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatbotRepository extends JpaRepository<ChatbotMessage, Long> {
}
