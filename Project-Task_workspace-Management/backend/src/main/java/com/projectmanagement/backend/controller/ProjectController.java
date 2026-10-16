package com.projectmanagement.backend.controller;
import com.projectmanagement.backend.entity.Project;
import com.projectmanagement.backend.service.ProjectService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/projects")

public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public Project createProject(@RequestBody Project project) {
        return projectService.saveProject(project);
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @PathVariable Long id,
            @RequestBody Project project) {

        try {

            Project updated =
                    projectService.updateProject(id, project);

            return ResponseEntity.ok(updated);

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {

        try {

            boolean deleted = projectService.deleteProject(id);

            if (deleted) {
                return ResponseEntity.ok(
                        "Project deleted successfully"
                );
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project not found");

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
}
