/*
 * TaskController: 클라이언트 요청을 받아서 TaskService를 통해 처리
 * /api/tasks 경로에서 GET, POST, PUT 등을 제공
 */
package ftt_backend.controller;

import ftt_backend.model.Task;
import ftt_backend.repository.TaskRepository;
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
    // 모든 Task 조회
    @GetMapping("")
    public ResponseEntity<?> getAllTasks() {
        // Task 목록을 조회해 반환
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // 새 Task 생성
    @PostMapping("")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        // 요청 바디에 담긴 Task 데이터를 DB에 저장
        Task createdTask = taskService.createTask(task);
        // 생성된 Task 엔티티를 JSON 형태로 반환
        return ResponseEntity.ok(createdTask);
    }

    // 단일 Task 조회: GET /api/tasks/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 Task가 존재하지 않습니다."));

        // task.files 에는 연관된 TaskFile 목록이 들어있어야 함 (EAGER라면 자동 로드)
        return ResponseEntity.ok(task);
    }

    //수정 모드
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody Task updatedTask) {
        Task task = taskService.updateTask(taskId, updatedTask);
        return ResponseEntity.ok(task);
    }

    //삭제 모드
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id); // 내부에서 taskRepository.deleteById(id)
        return ResponseEntity.ok("작업" + id + " 삭제.");
    }
    //다중 삭제하기
    @DeleteMapping("")
    public ResponseEntity<?> deleteTasks(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            taskService.deleteTask(id);
        }
        return ResponseEntity.ok("작업들 삭제 : " + ids);
    }
}
