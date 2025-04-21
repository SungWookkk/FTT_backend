package ftt_backend.repository;

import ftt_backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChannelIdOrderByTimestampAsc(Long channelId);
}
