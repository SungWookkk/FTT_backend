package ftt_backend.config.openai;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.model.Model;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAIConfig {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIConfig.class);

    @Bean
    public OpenAiService openAiService() {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        String apiKey = dotenv.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            logger.error("OPENAI_API_KEY가 .env 파일에 정의되어 있지 않습니다.");
            throw new IllegalStateException("환경변수 OPENAI_API_KEY를 확인해주세요.");
        }
        logger.info("✅ OPENAI API Key 로드 성공");

        OpenAiService service = new OpenAiService(apiKey);

        // 연결 테스트: 사용 가능한 모델 개수 로깅
        try {
            List<Model> models = service.listModels();
            logger.info("✅ OpenAI 연결 성공. 사용 가능한 모델 개수: {}", models.size());
        } catch (Exception e) {
            logger.error("❌ OpenAI 연결에 실패했습니다.", e);
        }

        return service;
    }
}
