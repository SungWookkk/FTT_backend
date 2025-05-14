// src/main/java/ftt_backend/FttBackendApplication.java
package ftt_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FttBackendApplication {

	public static void main(String[] args) {
		Dotenv twilioEnv = Dotenv.configure()
				.filename("twilio.env")
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();

		Dotenv socialEnv = Dotenv.configure()
				.filename("social.env")
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();

		String sid     = twilioEnv.get("TWILIO_ACCOUNT_SID");
		String token   = twilioEnv.get("TWILIO_AUTH_TOKEN");
		if (sid != null)   System.setProperty("TWILIO_ACCOUNT_SID", sid);
		if (token != null) System.setProperty("TWILIO_AUTH_TOKEN", token);
		System.setProperty("TWILIO_FROM_PHONE",   twilioEnv.get("TWILIO_FROM_PHONE", ""));
		System.setProperty("TWILIO_API_KEY_SID",  twilioEnv.get("TWILIO_API_KEY_SID", ""));
		System.setProperty("TWILIO_API_KEY_SECRET", twilioEnv.get("TWILIO_API_KEY_SECRET", ""));

		String googleId     = socialEnv.get("GOOGLE_CLIENT_ID");
		String googleSecret = socialEnv.get("GOOGLE_CLIENT_SECRET");
		if (googleId != null)     System.setProperty("GOOGLE_CLIENT_ID", googleId);
		if (googleSecret != null) System.setProperty("GOOGLE_CLIENT_SECRET", googleSecret);

		//  NAVER_CLIENT_ID 등도 동일하게 설정

		SpringApplication.run(FttBackendApplication.class, args);
	}
}
