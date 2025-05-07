package ftt_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
@RequestMapping("/")
@EnableScheduling
public class FttBackendApplication {

	public static void main(String[] args) {
		// 1) 프로젝트 루트에 있는 twilio.env 파일을 로드
		Dotenv dotenv = Dotenv.configure()
				.filename("twilio.env")    // 파일 이름
				.load();

		// 2) 트윌리오 관련 키/토큰을 System 환경변수로 설정
		System.setProperty("TWILIO_ACCOUNT_SID",  dotenv.get("TWILIO_ACCOUNT_SID"));
		System.setProperty("TWILIO_AUTH_TOKEN",   dotenv.get("TWILIO_AUTH_TOKEN"));
		System.setProperty("TWILIO_FROM_PHONE",   dotenv.get("TWILIO_FROM_PHONE"));
		System.setProperty("TWILIO_API_KEY_SID",  dotenv.get("TWILIO_API_KEY_SID",  ""));
		System.setProperty("TWILIO_API_KEY_SECRET", dotenv.get("TWILIO_API_KEY_SECRET", ""));

		SpringApplication.run(FttBackendApplication.class, args);
	}

}
