package ftt_backend.controller;

import ftt_backend.model.ChatMessage;
import ftt_backend.model.TeamChannel;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.ChatMessageRepository;
import ftt_backend.repository.TeamChannelRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private TeamChannelRepository teamChannelRepository;

    @Autowired
    private UserRepository userRepository;

    // 채널별 메시지 처리 (중요: DestinationVariable 사용)
    /**
     * 채널별 메시지 처리
     * 1) @DestinationVariable 로 넘어온 channelId
     * 2) @Payload 로 바인딩된 message.sender.id 에서 UserInfo 조회
     * 3) TeamChannel 조회
     * 4) timestamp 세팅, message.setChannel, message.setSender
     * 5) 저장 후 그대로 return → 클라이언트에게 브로드캐스트
     */
    @MessageMapping("/chat/send/{channelId}")
    @SendTo("/topic/chat/{channelId}")
    public Map<String,Object> send(
            @Payload ChatMessage message,
            @DestinationVariable Long channelId
    ) {
        // 1) 서버 타임스탬프 설정
        message.setTimestamp(LocalDateTime.now());

        // 2) sender(UserInfo) 실 엔티티 조회
        Long senderId = message.getSender().getId();
        UserInfo sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + senderId));
        message.setSender(sender);

        // 3) TeamChannel 존재 확인
        teamChannelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("채널을 찾을 수 없습니다: " + channelId));

        // 4) channelId 필드에 설정
        message.setChannelId(channelId);

        // 5) 저장
        ChatMessage saved = chatMessageRepository.save(message);

        // 6) 브로드캐스트할 payload 구성 (nested UserInfo 없이 단순 문자열 닉네임)
        return Map.of(
                "sender", sender.getUsername(),
                "content", saved.getContent(),
                "timestamp", saved.getTimestamp().toString(),
                "channelId", channelId
        );
    }

    @GetMapping("/api/channels/{teamId}")
    @ResponseBody
    public List<TeamChannel> getChannels(@PathVariable Long teamId) {
        return teamChannelRepository.findByTeamId(teamId);
    }
}