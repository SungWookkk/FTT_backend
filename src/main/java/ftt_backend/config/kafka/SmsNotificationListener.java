package ftt_backend.config.kafka;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.http.TwilioRestClient;
import ftt_backend.config.batch.dto.ReminderMessage;
import ftt_backend.repository.UserRepository;
import ftt_backend.model.UserInfo;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationListener {

    private final TwilioRestClient twilioClient;
    private final String fromPhone;
    @Autowired
    private UserRepository userRepo;

    public SmsNotificationListener(
            TwilioRestClient twilioClient,
            @Value("${twilio.from-phone}") String fromPhone,
            UserRepository userRepo
    ) {
        this.twilioClient = twilioClient;
        this.fromPhone    = fromPhone;
        this.userRepo     = userRepo;
    }

    @KafkaListener(topics = "task.deadline.sms", groupId = "todo-reminder")
    public void onReminderMessage(ReminderMessage msg) {
        // 1) DB 에 사용자 정보 다시 조회 (phoneNumber 로 lookup)
        userRepo.findByPhoneNumber(msg.getPhoneNumber())
                .filter(UserInfo::getSmsOptIn)         // 2) SMS 동의 여부 확인
                .ifPresent(user -> {
                    // 3) 실제 SMS 전송
                    String body = String.format(
                            "할 일 #%d \"%s\"의 마감일이 %s 입니다. 확인하세요!",
                            msg.getTaskId(),
                            msg.getTaskTitle(),
                            msg.getDueDate()
                    );
                    Message.creator(
                            new com.twilio.type.PhoneNumber(msg.getPhoneNumber()),
                            new com.twilio.type.PhoneNumber(fromPhone),
                            body
                    ).create(twilioClient);
                });
    }
}
