package ftt_backend.config.kafka;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.http.TwilioRestClient;
import com.twilio.type.PhoneNumber;
import ftt_backend.config.batch.dto.ReminderMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationListener {

    private final TwilioRestClient twilioClient;
    private final String fromPhone;

    public SmsNotificationListener(
            TwilioRestClient twilioClient,
            @Value("${twilio.from-phone}") String fromPhone
    ) {
        this.twilioClient = twilioClient;
        this.fromPhone    = fromPhone;
    }

    @KafkaListener(topics = "task.deadline.sms", groupId = "todo-reminder")
    public void onReminderMessage(ReminderMessage msg) {
        // ReminderMessage.phoneNumber 필드를 가져옵니다.
        String toPhone = msg.getPhoneNumber();

        String body = String.format(
                "할 일 #%d \"%s\" 의 마감일이 %s 입니다. 확인하세요!",
                msg.getTaskId(),
                msg.getTaskTitle(),
                msg.getDueDate()
        );

        Message.creator(
                new PhoneNumber(toPhone),   // 수신자 번호
                new PhoneNumber(fromPhone), // 발신자 번호
                body
        ).create(twilioClient);
    }
}
