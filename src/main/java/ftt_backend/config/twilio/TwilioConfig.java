package ftt_backend.config.twilio;

import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    // application.properties 에 선언한 값 주입
    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-phone}")
    private String fromPhone;

    @Value("${twilio.api-key-sid:}")
    private String apiKeySid;

    @Value("${twilio.api-key-secret:}")
    private String apiKeySecret;


    @Bean
    public TwilioRestClient twilioClient() {
        if (!apiKeySid.isBlank() && !apiKeySecret.isBlank()) {
            return new TwilioRestClient.Builder(apiKeySid, apiKeySecret)
                    .accountSid(accountSid).build();
        } else {
            return new TwilioRestClient.Builder(accountSid, authToken).build();
        }
    }

    /**
     * 발신번호를 편리하게 주입해두고,
     * 실제 SMS 서비스에서는 이 fromPhone 을 사용하세요.
     */
    @Bean
    public String twilioFromPhone() {
        return fromPhone;
    }
}
