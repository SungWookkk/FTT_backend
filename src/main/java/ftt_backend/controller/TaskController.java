package ftt_backend.controller;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import ftt_backend.config.JwtUtils;
import ftt_backend.config.openai.AIToDoService;
import ftt_backend.config.openai.dto.AIRequest;
import ftt_backend.config.openai.dto.TaskResponseDTO;
import ftt_backend.model.Task;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
import ftt_backend.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AIToDoService aiToDoService;

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private JwtUtils jwtUtils;
    // 모든 Task 조회
    @GetMapping("")
    public ResponseEntity<?> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // 새 Task 생성
    @PostMapping("")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.ok(createdTask);
    }

    // 단일 Task 조회
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 Task가 존재하지 않습니다."));
        return ResponseEntity.ok(task);
    }

    // "내 작업" 조회
    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks(
            @RequestParam(value = "userId", required = false, defaultValue = "") String userId) {
        if (userId.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        UserInfo user;
        try {
            // 먼저 userId를 숫자로 변환해 기본키(id)로 조회 시도
            Long id = Long.parseLong(userId);
            user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));
        } catch (NumberFormatException e) {
            // 숫자 변환이 안되면 로그인 아이디(문자열)로 조회
            user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. (userId: " + userId + ")"));
        }
        // 로그인 ID(문자열)를 기준으로 작업을 조회
        List<Task> myTasks = taskRepository.findByUser_UserId(user.getUserId());
        return ResponseEntity.ok(myTasks);
    }

    // 수정 Task
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody Task updatedTask) {
        Task task = taskService.updateTask(taskId, updatedTask);
        return ResponseEntity.ok(task);
    }

    // 단일 Task 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("작업 " + id + " 삭제.");
    }

    // 다중 Task 삭제
    @DeleteMapping("")
    public ResponseEntity<?> deleteTasks(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            taskService.deleteTask(id);
        }
        return ResponseEntity.ok("작업들 삭제 : " + ids);
    }



    // ────────────────────────────────────────────────────────
    // 1) 할 일 생성 전용: JSON 스펙을 파싱해서 Task 저장 (/api/tasks/ai-create)
    // ────────────────────────────────────────────────────────
    @PostMapping("/ai-create")
    public ResponseEntity<?> aiCreate(
            @RequestBody AIRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            // 1) 헤더 검사·토큰 추출
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization 헤더 필요");
            }
            String token = authorizationHeader.substring(7);
            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰");
            }
            String userId = jwtUtils.getAuthentication(token).getName();

            logger.info("▶ AI 생성 요청: userId={}, prompt={}", userId, request.getPrompt());
            Task created = aiToDoService.createTaskFromPrompt(request.getPrompt(), userId);

            // 5) 엔티티 → DTO
            TaskResponseDTO dto = new TaskResponseDTO();
            BeanUtils.copyProperties(created, dto);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            logger.error("AI 생성 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI 생성 중 오류: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────
    // 2) 자유 채팅 전용: 그냥 AI와 대화 (/api/tasks/chat)
    // ────────────────────────────────────────────────────────
    @PostMapping("/chat")
    public ResponseEntity<?> aiChat(
            @RequestBody AIRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            // 토큰 검증
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization 헤더 필요");
            }
            String token = authorizationHeader.substring(7);
            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰");
            }

            // ChatCompletionRequest 구성
            ChatCompletionRequest chatReq = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(List.of(
                            new ChatMessage("system", "당신은 친절한 AI 비서입니다."),
                            new ChatMessage("user", request.getPrompt())
                    ))
                    .build();

            // OpenAI 호출
            String answer = openAiService
                    .createChatCompletion(chatReq)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent()
                    .trim();

            // 단순 메시지 형태로 반환
            return ResponseEntity.ok(Map.of("message", answer));

        } catch (Exception e) {
            logger.error("AI 채팅 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI 채팅 중 오류: " + e.getMessage());
        }
    }
}