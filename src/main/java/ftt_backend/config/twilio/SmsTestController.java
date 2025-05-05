package ftt_backend.config.twilio;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.http.TwilioRestClient;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
public class SmsTestController {

    private final TwilioRestClient twilioClient;
    private final String fromPhone;

    public SmsTestController(
            TwilioRestClient twilioClient,
            @Value("${twilio.from-phone}") String fromPhone
    ) {
        this.twilioClient = twilioClient;
        this.fromPhone    = fromPhone;
    }

    /**
     * 호출 예시: POST http://localhost:8080/api/sms/test
     * Body (x-www-form-urlencoded):
     *   to   = +821071231906
     *   body = [원하는 테스트 메시지]
     */
    @PostMapping("/test")
    public ResponseEntity<String> sendTestSms(
            @RequestParam String to,
            @RequestParam(defaultValue = "테스트 문자입니다!") String body
    ) {
        Message msg = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromPhone),
                body
        ).create(twilioClient);

        return ResponseEntity.ok("Sent SID: " + msg.getSid());
    }
}
