package ftt_backend.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class PresenceController {

    private final SimpMessagingTemplate template;

    private final Map<Long, Set<Long>> onlineMap = new ConcurrentHashMap<>();

    @Autowired
    public PresenceController(SimpMessagingTemplate template) {
        this.template = template;
    }


    // 구독 시 즉시 현재 online 목록 리턴
    @SubscribeMapping("/topic/presence/{teamId}")
    public Set<Long> onSubscription(@DestinationVariable Long teamId) {
        return onlineMap.getOrDefault(teamId, Collections.emptySet());
    }

    // 입장 처리
    @MessageMapping("/presence/join")
    public void join(@Payload PresenceMessage msg) {
        if (msg == null || msg.getTeamId() == null || msg.getUserId() == null) {
            return;  // null 체크로 NPE 방지
        }
        onlineMap
                .computeIfAbsent(msg.getTeamId(), k -> ConcurrentHashMap.newKeySet())
                .add(msg.getUserId());
        template.convertAndSend(
                "/topic/presence/" + msg.getTeamId(),
                onlineMap.get(msg.getTeamId())
        );
    }

    // 퇴장 처리
    @MessageMapping("/presence/leave")
    public void leave(@Payload PresenceMessage msg) {
        if (msg == null || msg.getTeamId() == null || msg.getUserId() == null) {
            return;
        }
        Set<Long> set = onlineMap.get(msg.getTeamId());
        if (set != null) {
            set.remove(msg.getUserId());
            template.convertAndSend(
                    "/topic/presence/" + msg.getTeamId(),
                    set
            );
        }
    }
}
