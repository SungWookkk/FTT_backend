package ftt_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

@Component
public class EnvUtils {
    private final Dotenv dotenv;

    public EnvUtils() {
        this.dotenv = Dotenv.configure()
                .directory("./") // .env 파일 경로 (프로젝트 루트 디렉토리)
                .ignoreIfMalformed() // 파일이 잘못된 경우 무시
                .ignoreIfMissing() // .env 파일이 없으면 무시
                .load();
    }

    public String get(String key) {
        return dotenv.get(key);
    }
}
