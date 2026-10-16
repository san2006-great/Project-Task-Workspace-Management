package com.projectmanagement.backend.service;

import com.projectmanagement.backend.entity.User;
import com.projectmanagement.backend.entity.Workspace;
import com.projectmanagement.backend.repository.UserRepository;
import com.projectmanagement.backend.repository.WorkspaceRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            UserRepository userRepository) {

        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    public Workspace saveWorkspace(Workspace workspace) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        workspace.setUser(user);

        return workspaceRepository.save(workspace);
    }

    public List<Workspace> getAllWorkspaces() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        return workspaceRepository.findByUser(user);
    }

    public Workspace updateWorkspace(Long id, Workspace updatedWorkspace) {

        Workspace workspace =
                workspaceRepository.findById(id).orElse(null);

        if (workspace == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Workspace not found"
            );
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        if (workspace.getUser() == null ||
                !workspace.getUser().getId().equals(user.getId())) {

            System.out.println(
                    "WORKSPACE OWNERSHIP DENIED: workspaceUser="
                            + workspace.getUser().getId()
                            + " | currentUser="
                            + user.getId()
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to modify this workspace"
            );
        }

        workspace.setName(updatedWorkspace.getName());
        workspace.setDescription(updatedWorkspace.getDescription());

        return workspaceRepository.save(workspace);
    }

    public boolean deleteWorkspace(Long id) {

        Workspace workspace =
                workspaceRepository.findById(id).orElse(null);

        if (workspace == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Workspace not found"
            );
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        if (workspace.getUser() == null ||
                !workspace.getUser().getId().equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to delete this workspace"
            );
        }

        workspaceRepository.deleteById(id);

        return true;
    }
}