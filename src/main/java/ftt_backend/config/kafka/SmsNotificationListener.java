package ftt_backend.config.kafka;

import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import ftt_backend.config.batch.dto.ReminderMessage;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.UserRepository;
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
        this.fromPhone = fromPhone;
        this.userRepo = userRepo;
    }

    @KafkaListener(
            topics = "task.deadline.sms",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onReminderMessage(ReminderMessage msg) {
        System.out.println(">> KafkaListener 호출됨: " + msg);
        userRepo.findByPhoneNumber(msg.getPhoneNumber())
                .filter(UserInfo::getSmsOptIn)
                .ifPresent(user -> {
                    String raw = msg.getPhoneNumber();
                    String to = raw.startsWith("+") ? raw : raw.replaceFirst("^0", "+82");
                    String body = String.format(
                            "할 일 #%d “%s” 마감일이 %s 입니다. 마감일이 얼마 남지 않았어요!",
                            msg.getTaskId(), msg.getTaskTitle(), msg.getDueDate()
                    );

                    try {
                        Message tw = Message.creator(
                                        new PhoneNumber(to),
                                        new PhoneNumber(fromPhone),
                                        body
                                )
                                .create(twilioClient);
                        System.out.println(">> Twilio 전송 성공, SID=" + tw.getSid());
                    } catch (Exception e) {
                        System.err.println("!! Twilio 전송 에러");
                        e.printStackTrace();
                    }
                });
    }
}
