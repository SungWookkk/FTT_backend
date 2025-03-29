/**
 * DataLoader 클래스는 서버 실행 시 자동으로 기본 사용자 계정을 생성하기 위해 사용
 */
package ftt_backend.config.dataloader;
import ftt_backend.model.UserInfo;
import ftt_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataLoader implements CommandLineRunner {
    @Autowired
    private UserService userService;

    // 서버 실행 시작 후 실행되는 메서드
    @Override
    public void run(String... args) throws Exception {
        // UserInfo 엔티티 인스턴스를 생성
        UserInfo user = new UserInfo();
        user.setUserId("test");
        user.setUsername("test");
        user.setEmail("qw@12");
        user.setPhoneNumber("12341232132");
        user.setBirthDate(generateRandomBirthDate());
        user.setPassword("1234");
        user.setRole("USER");
        // UserService를 사용하여 사용자 정보를 저장
        userService.saveUser(user);

        // 서버 콘솔에 기본 사용자 계정 생성 로그를 출력
        System.out.println("기본 사용자 계정이 생성되었습니다: " + user.toString());
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
