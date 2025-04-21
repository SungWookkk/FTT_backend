package ftt_backend.controller;

import ftt_backend.model.ChatMessage;
import ftt_backend.model.TeamChannel;
import ftt_backend.repository.ChatMessageRepository;
import ftt_backend.repository.TeamChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private TeamChannelRepository teamChannelRepository;

    // 채널별 메시지 처리 (중요: DestinationVariable 사용)
    @MessageMapping("/chat/send/{channelId}")
    @SendTo("/topic/chat/{channelId}")
    public ChatMessage send(ChatMessage message, @DestinationVariable Long channelId) {
        // 서버 타임스탬프 설정
        message.setTimestamp(LocalDateTime.now());
        // 채널 ID 설정 (필요시 message 객체에 channelId 필드 추가 필요)
        // message.setChannelId(channelId);
        // DB에 저장
        chatMessageRepository.save(message);
        return message;
    }

    @GetMapping("/api/channels/{teamId}")
    @ResponseBody
    public List<TeamChannel> getChannels(@PathVariable Long teamId) {
        return teamChannelRepository.findByTeamId(teamId);
    }
}