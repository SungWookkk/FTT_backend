package ftt_backend.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic smsReminderTopic() {
        return TopicBuilder.name("task.deadline.sms")
                .partitions(1)
                .replicas(1)
                .build();
    }
}