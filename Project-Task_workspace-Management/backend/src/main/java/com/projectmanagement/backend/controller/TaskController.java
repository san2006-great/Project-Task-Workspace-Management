package com.projectmanagement.backend.controller;

import com.projectmanagement.backend.entity.Task;
import com.projectmanagement.backend.service.TaskService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Create task
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {

        Task createdTask = taskService.saveTask(task);

        if (createdTask != null) {
            return ResponseEntity.ok(createdTask);
        }

        return ResponseEntity
                .status(403)
                .body("You are not allowed to create a task in this project");
    }

    // Get all tasks
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
    // Get task by ID
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        try {
            return ResponseEntity.ok(
                    taskService.updateStatus(id, status)
            );

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    // Update complete task
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long id,
            @RequestBody Task task) {

        try {
            return ResponseEntity.ok(
                    taskService.updateTask(id, task)
            );

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
    // Assign task to user
    @PutMapping("/{taskId}/assignee/{userId}")
    public ResponseEntity<?> assignTask(
            @PathVariable Long taskId,
            @PathVariable Long userId) {

        try {
            return ResponseEntity.ok(
                    taskService.assignTask(taskId, userId)
            );

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    // Delete task
    // Delete task
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {

        try {
            taskService.deleteTask(id);

            return ResponseEntity.ok(
                    "Task deleted successfully"
            );

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
}