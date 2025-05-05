package ftt_backend.config.twilio;

import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Value("${twilio.account-sid}")
    private String accountSid;
    @Value("${twilio.auth-token}")
    private String authToken;
    @Value("${twilio.api-key-sid:}")
    private String apiKeySid;
    @Value("${twilio.api-key-secret:}")
    private String apiKeySecret;

    /** Spring Bean 으로 TwilioRestClient 생성 */
    @Bean
    public TwilioRestClient twilioClient() {
        if (!apiKeySid.isBlank() && !apiKeySecret.isBlank()) {
            return new TwilioRestClient.Builder(apiKeySid, apiKeySecret)
                    .accountSid(accountSid)
                    .build();
        }
        return new TwilioRestClient.Builder(accountSid, authToken).build();
    }

    /** 애플리케이션 시작 시 전역 Twilio.init 호출 */
    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }
}
