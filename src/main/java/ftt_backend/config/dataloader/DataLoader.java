/**
 * DataLoader 클래스는 서버 실행 시 자동으로 기본 사용자 계정을 생성하기 위해 사용
 */
package ftt_backend.config.dataloader;
import ftt_backend.model.Task;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TaskRepository;
import ftt_backend.service.TaskService;
import ftt_backend.service.UserService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataLoader implements CommandLineRunner {
    @Autowired
    private UserService userService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JobLauncher jobLauncher;

    /** BatchConfig 에서 정의한 ’smsReminderJob’ 을 주입 */
    @Autowired
    @Qualifier("smsReminderJob")
    private Job smsReminderJob;

    // 서버 실행 시작 후 실행되는 메서드

    @Override
    public void run(String... args) throws Exception {
        // === 1) 기본 사용자 계정 생성 (중복 방지 & 기존 사용자 로드) ===
        UserInfo defaultUser = new UserInfo();
        defaultUser.setUserId("test");
        defaultUser.setUsername("test");
        defaultUser.setEmail("test@example.com");
        defaultUser.setBirthDate("1990-01-01");
        defaultUser.setPhoneNumber("+821071231906");
        defaultUser.setPassword("1234");
        defaultUser.setRole("USER");
        defaultUser.setSmsOptIn(true);

        UserInfo savedUser;
        try {
            savedUser = userService.saveUser(defaultUser);    // 수정: saveUser가 UserInfo를 반환하도록 처리
            System.out.println(">> 기본 사용자 생성: " + savedUser);
        } catch (DataIntegrityViolationException ex) {
            savedUser = userService.findByPhoneNumber(defaultUser.getPhoneNumber());
            System.out.println(">> 기본 사용자 이미 존재, 불러오기: " + savedUser);
        }

        // 2) ‘내일’ 마감인 테스트 Task 하나 생성
        Task task = new Task();
        task.setUser(savedUser);
        task.setTitle("자동생성 테스트 과제");
        task.setDescription("DataLoader 로 생성된 과제입니다.");
        task.setCreatedAt(LocalDate.now());
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setPriority("보통");
        task.setStatus("진행중");
        task.setAssignee("홍길동");
        task.setMemo("자동으로 만든 메모");
        taskRepository.save(task);

        System.out.println(">> 테스트 Task 생성: " + task);

        // 3) BatchJob 실행 → Kafka 에 ReminderMessage 발행 → SMS 리스너가 문자 발송
        try {
            jobLauncher.run(
                    smsReminderJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * generateRandomBirthDate() 메서드는 1970-01-01부터 2000-12-31 사이의 랜덤한 날짜를 생성하여 문자열(yyyy-MM-dd 형식)로 반환
     * 이 방법은 사용자 생년월일에 다양성을 부여 테스트 데이터로 활용할 수 있음
     *
     * @return 랜덤으로 생성된 생년월일 문자열 (yyyy-MM-dd)
     */
    private String generateRandomBirthDate() {
        // 시작 날짜(1970-01-01)를 epoch day 형식으로 변환
        long startEpochDay = LocalDate.of(1970, 1, 1).toEpochDay();
        // 종료 날짜(2000-12-31)를 epoch day 형식으로 변환
        long endEpochDay = LocalDate.of(2000, 12, 31).toEpochDay();
        // 시작 날짜와 종료 날짜 사이의 랜덤한 epoch day 값을 생성.
        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        // 생성된 랜덤 epoch day 값을 LocalDate 객체로 변환
        LocalDate randomDate = LocalDate.ofEpochDay(randomDay);
        // LocalDate 객체를 기본 ISO 형식(yyyy-MM-dd) 문자열로 반환
        return randomDate.toString();
    }
}
