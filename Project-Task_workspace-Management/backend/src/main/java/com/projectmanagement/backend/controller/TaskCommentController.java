package com.projectmanagement.backend.controller;

import com.projectmanagement.backend.entity.TaskComment;
import com.projectmanagement.backend.service.TaskCommentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    public TaskCommentController(TaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    // Get all comments for a task
    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long taskId) {

        try {

            List<TaskComment> comments =
                    taskCommentService.getComments(taskId);

            return ResponseEntity.ok(comments);

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of(
                            "message",
                            e.getReason() != null
                                    ? e.getReason()
                                    : "Request failed"
                    ));
        }
    }

    // Add comment
    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> request) {

        try {

            String comment = request.get("comment");

            TaskComment createdComment =
                    taskCommentService.addComment(
                            taskId,
                            comment
                    );

            return ResponseEntity.ok(createdComment);

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of(
                            "message",
                            e.getReason() != null
                                    ? e.getReason()
                                    : "Request failed"
                    ));
        }
    }

    // Update own comment
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {

        try {

            String comment = request.get("comment");

            TaskComment updatedComment =
                    taskCommentService.updateComment(
                            commentId,
                            comment
                    );

            return ResponseEntity.ok(updatedComment);

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of(
                            "message",
                            e.getReason() != null
                                    ? e.getReason()
                                    : "Request failed"
                    ));
        }
    }

    // Delete comment
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId) {

        try {

            taskCommentService.deleteComment(commentId);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Comment deleted successfully"
                    )
            );

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of(
                            "message",
                            e.getReason() != null
                                    ? e.getReason()
                                    : "Request failed"
                    ));
        }
    }
}