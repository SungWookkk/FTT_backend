/*
 * TaskController: 클라이언트 요청을 받아서 TaskService를 통해 처리
 * /api/tasks 경로에서 GET, POST, PUT 등을 제공
 */
package ftt_backend.controller;

import ftt_backend.model.Task;
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

    // Task 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        // id에 해당하는 Task를 updatedTask의 필드로 수정
        Task task = taskService.updateTask(id, updatedTask);
        // 수정된 Task 엔티티를 JSON 형태로 반환
        return ResponseEntity.ok(task);
    }
}
