package ftt_backend.config.kafka;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.http.TwilioRestClient;
import ftt_backend.config.batch.dto.ReminderMessage;
import ftt_backend.repository.UserRepository;
import ftt_backend.model.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationListener {

    private final TwilioRestClient twilioClient;
    private final String fromPhone;
    private final UserRepository userRepo;

    public SmsNotificationListener(
            TwilioRestClient twilioClient,
            @Value("${twilio.from-phone}") String fromPhone,
            UserRepository userRepo
    ) {
        this.twilioClient = twilioClient;
        this.fromPhone    = fromPhone;
        this.userRepo     = userRepo;
    }

    @KafkaListener(
            topics = "task.deadline.sms",
            containerFactory = "kafkaListenerContainerFactory",
            groupId = "todo-reminder"
    )
    public void onReminderMessage(ReminderMessage msg) {
        userRepo.findByPhoneNumber(msg.getPhoneNumber())
                .filter(UserInfo::getSmsOptIn)
                .ifPresent(user -> {
                    // E.164 포맷 보정 (0101234→+82101234)
                    String raw = msg.getPhoneNumber();
                    String to  = raw.startsWith("+") ? raw
                            : raw.replaceFirst("^0", "+82");

                    String body = String.format(
                            "할 일 #%d “%s” 마감일이 %s 입니다.",
                            msg.getTaskId(), msg.getTaskTitle(), msg.getDueDate()
                    );

                    Message.creator(
                                    new com.twilio.type.PhoneNumber(to),
                                    new com.twilio.type.PhoneNumber(fromPhone),
                                    body
                            )
                            // 전역 초기화했으므로 인자 없이도, 또는 twilioClient 전달
                            .create(twilioClient);
                });
    }
}
