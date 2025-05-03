package ftt_backend.config.batch;

import ftt_backend.config.batch.dto.ReminderMessage;
import ftt_backend.model.Task;
import ftt_backend.repository.TaskRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing  // JobLauncher, JobRepository, TransactionManager 자동 등록
@EnableScheduling
public class BatchConfig {

    /** SB5 에서는 이 두 빈을 직접 주입 받아, 즉시 사용 */
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private KafkaTemplate<String, ReminderMessage> kafkaTemplate;


    /** 1) Job 정의 */
    @Bean
    public Job smsReminderJob(Step smsStep) {
        return new JobBuilder("smsReminderJob", jobRepository)
                .start(smsStep)
                .build();
    }

    /** 2) Step 정의 */
    @Bean
    public Step smsStep(
            ItemReader<Task> reader,
            ItemProcessor<Task, ReminderMessage> processor,
            ItemWriter<ReminderMessage> writer
    ) {
        // StepBuilder 에서 chunk() 의 트랜잭션 매니저도 함께 지정
        @SuppressWarnings("unchecked")
        SimpleStepBuilder<Task,ReminderMessage> builder =
                new StepBuilder("smsStep", jobRepository)
                        .<Task,ReminderMessage>chunk(100, transactionManager);

        return builder
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    /** 3) RepositoryItemReader: 마감 임박한 Task 조회 */
    @Bean
    public ItemReader<Task> reader() {
        LocalDate today    = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        return new RepositoryItemReaderBuilder<Task>()
                .name("taskReader")
                .repository(taskRepository)
                .methodName("findImminentTasks")
                .arguments(List.of(today, tomorrow))
                .pageSize(100)
                .sorts(Map.of("dueDate", Sort.Direction.ASC))
                .build();
    }

    /** 4) Processor: SMS 수신 동의 체크 후 DTO 생성 */
    @Bean
    public ItemProcessor<Task, ReminderMessage> processor() {
        return task -> {
            if (Boolean.TRUE.equals(task.getUser().getSmsOptIn())) {
                return new ReminderMessage(
                        task.getUser().getId(),
                        task.getUser().getPhoneNumber(),
                        task.getId(),
                        task.getTitle(),
                        task.getDueDate()
                );
            }
            return null;
        };
    }

    /** 5) Writer: Kafka 에 메시지 발행 */
    @Bean
    public ItemWriter<ReminderMessage> writer() {
        return messages -> {
            for (ReminderMessage msg : messages) {
                if (msg != null) {
                    kafkaTemplate.send("task.deadline.sms", msg);
                }
            }
        };
    }

    /**
     * 6) 스케줄: 매일 새벽 00:10 에 배치 실행
     *    (Scheduled 메서드는 인자·반환값 없이 정의)
     */
    @Scheduled(cron = "0 10 0 * * *")
    public void runSmsJob() throws Exception {
        jobLauncher.run(
                smsReminderJob(smsStep(reader(), processor(), writer())),
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );
    }
}
