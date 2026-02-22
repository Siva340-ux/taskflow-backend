package com.internship.app.controller;

import com.internship.app.model.Task;
import com.internship.app.model.User;
import com.internship.app.repository.TaskRepository;
import com.internship.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ✅ DTOs - NO CIRCULAR REFERENCES
record TaskResponse(Long id, String title, String description, Boolean completed) {}
record CreateTaskRequest(String title, String description) {}

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks() {
        User currentUser = getCurrentUser();
        log.info("Fetching tasks for user: {}", currentUser.getEmail());

        List<Task> tasks = taskRepository.findByUserId(currentUser.getId());
        List<TaskResponse> response = tasks.stream()
                .map(task -> new TaskResponse(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getCompleted()
                ))
                .toList();

        log.info("Returning {} tasks", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody CreateTaskRequest request) {
        User currentUser = getCurrentUser();
        log.info("Creating task for user: {}", currentUser.getEmail());

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setUser(currentUser);
        task.setCompleted(false);

        Task saved = taskRepository.save(task);

        TaskResponse response = new TaskResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getCompleted()
        );

        log.info("Created task ID: {}", saved.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody CreateTaskRequest request) {
        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        Task saved = taskRepository.save(task);

        return ResponseEntity.ok(new TaskResponse(
                saved.getId(), saved.getTitle(), saved.getDescription(), saved.getCompleted()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        taskRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new RuntimeException("Invalid user principal");
    }
}
