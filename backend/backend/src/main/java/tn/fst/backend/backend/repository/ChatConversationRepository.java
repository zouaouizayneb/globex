package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.ChatConversation;
import tn.fst.backend.backend.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    List<ChatConversation> findByUserOrderByUpdatedAtDesc(User user);

    Optional<ChatConversation> findByIdConversationAndUser(Long idConversation, User user);

    @Query("SELECT c FROM ChatConversation c WHERE c.user = :user ORDER BY c.updatedAt DESC")
    List<ChatConversation> findRecentConversationsByUser(@Param("user") User user);

    boolean existsByUserAndTitle(User user, String title);
}
