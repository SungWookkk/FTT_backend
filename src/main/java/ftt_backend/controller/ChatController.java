package ftt_backend.controller;

import ftt_backend.model.ChatMessage;
import ftt_backend.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat/send")
    @SendTo("/topic/chat")
    public ChatMessage send(ChatMessage message) {
        // 서버 타임스탬프 설정
        message.setTimestamp(LocalDateTime.now());
        // DB에 저장
        chatMessageRepository.save(message);
        return message;
    }
}
