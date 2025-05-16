package ftt_backend.config.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import ftt_backend.config.openai.dto.TaskSpecDTO;
import ftt_backend.model.Task;
import ftt_backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 프롬프트를 받아 Task JSON 스펙을 생성하고,
 * DTO를 엔티티로 변환 후 DB에 저장하는 서비스
 */
@Service
@RequiredArgsConstructor
public class AIToDoService {

    private static final Logger logger = LoggerFactory.getLogger(AIToDoService.class);

    private final OpenAiService openAi;
    private final TaskService taskService;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 사용자 프롬프트에 따라 Task를 생성
     * @param prompt AI에게 전달할 자연어 프롬프트
     * @param userId 인증된 사용자 ID
     * @return 저장된 Task 엔티티
     */
    public Task createTaskFromPrompt(String prompt, String userId) throws Exception {
        logger.info("▶ AIToDoService.createTaskFromPrompt 호출. userId={}, prompt={}", userId, prompt);

        // 시스템 메시지: JSON 스펙 양식 안내
        String systemMsg = """
            당신은 ToDo List API를 위한 JSON 스펙 생성기입니다.
            실제 작업 데이터를 기준으로 반드시 유효한 ISO 날짜(YYYY-MM-DD)만 리턴하세요.
            틀린 포맷이나 placeholder("YYYY-MM-DD") 절대 사용 금지.
            사용자가 "담당자는"이라는 말을 하면 그 사용자의 닉네임을 추출해서 데이터입력을 하세요.
            {
              "title":"<제목>",
              "description":"<HTML 가능>",
              "priority":"낮음|보통|높음",
              "startDate":"2025-05-16",
              "dueDate":"2025-05-16",
              "assignee":"담당자 이름",
              "memo":"추가 메모"
            }
            """;

        ChatCompletionRequest req = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(
                        new ChatMessage("system", systemMsg),
                        new ChatMessage("user", prompt)
                ))
                .temperature(0.2)
                .maxTokens(300)
                .build();

        // OpenAI 호출 전 디버그 로그
        logger.debug("▶ OpenAI 호출 요청: {}", req);
        String jsonSpec = openAi.createChatCompletion(req)
                .getChoices().get(0).getMessage().getContent().trim();

        // AI 응답 원문 로깅
        logger.info("▶ AI raw response: {}", jsonSpec);

        // JSON > DTO
        TaskSpecDTO spec = mapper.readValue(jsonSpec, TaskSpecDTO.class);
        logger.debug("▶ 파싱된 TaskSpecDTO: {}", spec);

        // 엔티티 변환 준비
        Task task = new Task();
        task.setTitle(spec.getTitle());
        task.setDescription(spec.getDescription());
        task.setPriority(spec.getPriority());

        // 날짜 필드 검증
        String sd = spec.getStartDate();
        if (sd != null && sd.matches("\\d{4}-\\d{2}-\\d{2}")) {
            task.setStartDate(LocalDate.parse(sd));
        } else {
            logger.warn("▶ startDate 파싱 스킵: {}", sd);
        }
        String dd = spec.getDueDate();
        if (dd != null && dd.matches("\\d{4}-\\d{2}-\\d{2}")) {
            task.setDueDate(LocalDate.parse(dd));
        } else {
            logger.warn("▶ dueDate 파싱 스킵: {}", dd);
        }

        task.setAssignee(spec.getAssignee());
        task.setMemo(spec.getMemo());
        task.setUserId(userId);

        logger.info("▶ 저장할 Task 엔티티: {}", task);

        // DB 저장
        Task saved = taskService.createTask(task);
        logger.info("✅ AI 생성 Task 저장 완료: id={}", saved.getId());
        return saved;
    }
}
