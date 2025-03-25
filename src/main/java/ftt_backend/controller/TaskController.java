package ftt_backend.controller;

import ftt_backend.model.Task;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
import ftt_backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

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
}
