package ftt_backend.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SmsReminderScheduler {

    private final JobLauncher jobLauncher;
    private final Job smsReminderJob;

    @Autowired
    public SmsReminderScheduler(
            JobLauncher jobLauncher,
            @Qualifier("smsReminderJob") Job smsReminderJob
    ) {
        this.jobLauncher = jobLauncher;
        this.smsReminderJob = smsReminderJob;
    }

    /**
     * 매일 오전 9시에 smsReminderJob 실행
     * (cron 표현식: 초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void runDailySmsReminder() {
        try {
            jobLauncher.run(
                    smsReminderJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())  // 매번 새로운 파라미터가 필요
                            .toJobParameters()
            );
            System.out.println(">> 스케줄러: smsReminderJob 실행 완료");
        } catch (Exception e) {
            System.err.println("!! 스케줄러: smsReminderJob 실행 중 에러");
            e.printStackTrace();
        }
    }
}
