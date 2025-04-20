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
                .addEndpoint("/ws") //엔드포인트로 클라이언트 연결을 받고 SockJS 폴백 지원
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 메시지 브로커 설정: application-prefix 와 브로커 목적지.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트로 보내는 토픽/큐
        registry.enableSimpleBroker("/topic", "/queue"); // 브로커를 통해 메시지 전달
        // 클라이언트가 메시지 보낼 때 앞에 붙이는 접두사
        registry.setApplicationDestinationPrefixes("/app"); //접두사로 들어오는 메시지를 처리(컨트롤러 매핑)
    }
}
