package com.projectmanagement.backend.controller;
import com.projectmanagement.backend.entity.Workspace;
import com.projectmanagement.backend.service.WorkspaceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/workspaces")

public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public Workspace createWorkspace(@RequestBody Workspace workspace) {
        return workspaceService.saveWorkspace(workspace);
    }

    @GetMapping
    public List<Workspace> getAllWorkspaces() {
        return workspaceService.getAllWorkspaces();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkspace(
            @PathVariable Long id,
            @RequestBody Workspace workspace) {

        try {
            Workspace updated =
                    workspaceService.updateWorkspace(id, workspace);

            return ResponseEntity.ok(updated);

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWorkspace(@PathVariable Long id) {

        try {
            boolean deleted = workspaceService.deleteWorkspace(id);

            if (deleted) {
                return ResponseEntity.ok("Workspace deleted successfully");
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Workspace not found");

        } catch (ResponseStatusException e) {

            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
}
