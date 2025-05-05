package ftt_backend.task;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import ftt_backend.config.batch.dto.ReminderMessage;
import ftt_backend.config.kafka.SmsNotificationListener;
import ftt_backend.model.Task;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import com.twilio.http.TwilioRestClient;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SMS 리마인더 전체 플로우를 검증하는 통합 테스트
 */
@SpringBootTest(properties = {
        "twilio.account-sid=",
        "twilio.auth-token=",
        "twilio.from-phone=+",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.kafka.admin.enabled=false",
        "spring.kafka.admin.auto-create-topics=false",
        "spring.kafka.admin.fail-fast=false",
        "spring.kafka.bootstrap-servers=none:9092",
        // MySQL 실 DB
        "spring.datasource.url=jdbc:mysql://localhost:3306/fft?useSSL=false&allowPublicKeyRetrieval=true",
        "spring.datasource.username=",
        "spring.datasource.password=!",
        // Batch 스키마만 실행
        "spring.batch.jdbc.initialize-schema=always",
        "spring.sql.init.mode=never"
})
public class SmsReminderFlowTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("smsReminderJob")
    private Job smsReminderJob;

    @Autowired
    private SmsNotificationListener smsNotificationListener;

    @MockBean
    private KafkaTemplate<String, ReminderMessage> kafkaTemplate;  // Kafka 퍼블리시 목업

    @MockBean
    private TwilioRestClient twilioClient;  // Twilio API 호출 목업

    @Test
    void testSmsReminderJob_publishesToKafka() throws Exception {
        // --- 필수 필드 모두 세팅 ---
        UserInfo user = new UserInfo();
        user.setBirthDate("1990-01-01");
        user.setUserId("testuser");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setSmsOptIn(true);
        user.setPhoneNumber("+");
        user = userRepository.save(user);

        Task task = new Task();
        task.setTitle("테스트 과제");
        task.setDescription("테스트 설명");
        task.setCreatedAt(LocalDate.now());
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setPriority("보통");
        task.setStatus("진행중");
        task.setAssignee("홍길동");
        task.setMemo("테스트 메모");
        task.setUser(user);
        taskRepository.save(task);

        jobLauncher.run(
                smsReminderJob,
                new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters()
        );

        verify(kafkaTemplate, times(1))
                .send(eq("task.deadline.sms"), any(ReminderMessage.class));
    }

    @Test
    void testSmsNotificationListener_sendsSmsViaTwilio() {
        ReminderMessage msg = new ReminderMessage(
                1L, "+", 100L, "테스트 과제", LocalDate.now().plusDays(1)
        );

        UserRepository mockRepo = mock(UserRepository.class);
        UserInfo dummy = new UserInfo();
        dummy.setSmsOptIn(true);
        dummy.setPhoneNumber(msg.getPhoneNumber());
        when(mockRepo.findByPhoneNumber(msg.getPhoneNumber()))
                .thenReturn(Optional.of(dummy));

        String fromPhone = "+19786622051";
        SmsNotificationListener listener =
                new SmsNotificationListener(twilioClient, fromPhone, mockRepo);

        try (MockedStatic<Message> msgStatic = mockStatic(Message.class)) {
            MessageCreator creator = mock(MessageCreator.class);
            // 모든 PhoneNumber와 String에 대해 스텁이 동작하도록 변경
            msgStatic.when(() ->
                    Message.creator(
                            any(com.twilio.type.PhoneNumber.class),
                            any(com.twilio.type.PhoneNumber.class),
                            anyString()
                    )
            ).thenReturn(creator);
            when(creator.create(twilioClient)).thenReturn(mock(Message.class));

            listener.onReminderMessage(msg);

            // 검증도 any 매처에 맞춰 수행
            msgStatic.verify(() ->
                    Message.creator(
                            any(com.twilio.type.PhoneNumber.class),
                            any(com.twilio.type.PhoneNumber.class),
                            contains(msg.getTaskTitle())
                    ), times(1)
            );
        }
    }
}