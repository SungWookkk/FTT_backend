package ftt_backend.socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 클라이언트가 WebSocket 연결을 생성할 엔드포인트를 등록
     * SockJS fallback 지원을 위해 withSockJS() 호출
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // CORS 필요하다면 도메인 지정
                .withSockJS();
    }

    /**
     * 메시지 브로커를 설정
     * 클라이언트는 /app/** 로 메시지를 보내고
     * 서버는 /topic/** 을 통해 구독자에게 전달
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config
                .setApplicationDestinationPrefixes("/app")
                .enableSimpleBroker("/topic");
    }
}
