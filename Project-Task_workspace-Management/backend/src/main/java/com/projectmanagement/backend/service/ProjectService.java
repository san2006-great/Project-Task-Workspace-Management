package com.projectmanagement.backend.service;

import com.projectmanagement.backend.entity.Project;
import com.projectmanagement.backend.entity.User;
import com.projectmanagement.backend.entity.Workspace;
import com.projectmanagement.backend.repository.ProjectRepository;
import com.projectmanagement.backend.repository.UserRepository;
import com.projectmanagement.backend.repository.WorkspaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            WorkspaceRepository workspaceRepository) {

        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public Project saveProject(Project project) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        if (project.getWorkspace() == null ||
                project.getWorkspace().getId() == null) {
            return null;
        }

        Workspace workspace = workspaceRepository
                .findById(project.getWorkspace().getId())
                .orElse(null);

        if (workspace == null ||
                workspace.getUser() == null ||
                !workspace.getUser().getId().equals(user.getId())) {
            return null;
        }

        project.setWorkspace(workspace);

        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        return projectRepository.findByWorkspace_User(user);
    }
    public Project updateProject(Long id, Project updatedProject) {

        Project project =
                projectRepository.findById(id).orElse(null);

        if (project == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Project not found"
            );
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        if (project.getWorkspace() == null ||
                project.getWorkspace().getUser() == null ||
                !project.getWorkspace().getUser().getId().equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to modify this project"
            );
        }

        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());

        return projectRepository.save(project);
    }

    public boolean deleteProject(Long id) {

        Project project =
                projectRepository.findById(id).orElse(null);

        if (project == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Project not found"
            );
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        if (project.getWorkspace() == null ||
                project.getWorkspace().getUser() == null ||
                !project.getWorkspace().getUser().getId().equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to delete this project"
            );
        }

        projectRepository.deleteById(id);

        return true;
    }
}