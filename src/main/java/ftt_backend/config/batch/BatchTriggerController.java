package ftt_backend.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batch")
public class BatchTriggerController {

    private final JobLauncher jobLauncher;
    private final Job smsReminderJob;

    @Autowired
    public BatchTriggerController(
            JobLauncher jobLauncher,
            @Qualifier("smsReminderJob") Job smsReminderJob
    ) {
        this.jobLauncher   = jobLauncher;
        this.smsReminderJob = smsReminderJob;
    }

    @PostMapping("/run-sms-reminder")
    public ResponseEntity<String> runSmsReminderNow() throws Exception {
        jobLauncher.run(
                smsReminderJob,
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );
        return ResponseEntity.ok("SMS Reminder Job triggered");
    }
}
