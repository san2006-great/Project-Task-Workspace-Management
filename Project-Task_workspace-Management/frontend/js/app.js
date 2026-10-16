let allTasks = [];
console.log("app.js is working");
const API_URL = "http://localhost:8080";
const userRole = localStorage.getItem("role");
const userId = localStorage.getItem("userId");

if (userRole !== "ADMIN") {

    const taskAssigneeField =
        document.getElementById("taskAssigneeField");

    if (taskAssigneeField) {
        taskAssigneeField.style.display = "none";
    }

    const editTaskAssignee =
        document.getElementById("editTaskAssignee");

    const editTaskAssigneeLabel =
        document.querySelector(
            'label[for="editTaskAssignee"]'
        );

    if (editTaskAssignee) {
        editTaskAssignee.style.display = "none";
    }

    if (editTaskAssigneeLabel) {
        editTaskAssigneeLabel.style.display = "none";
    }
}


if (userRole !== "ADMIN") {
    const userManagementLink =
        document.getElementById("userManagementLink");

    if (userManagementLink) {
        userManagementLink.style.display = "none";
    }
}

// ================= REGISTER =================

const registerForm = document.getElementById("registerForm");

if (registerForm) {

    registerForm.addEventListener("submit", function(event) {

        event.preventDefault();

        const name =
            document.getElementById("name").value;

        const email =
            document.getElementById("email").value;

        const password =
            document.getElementById("password").value;

        const role =
            document.getElementById("role").value;

        const message =
            document.getElementById("message");

        fetch(API_URL + "/api/users", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                name: name,
                email: email,
                password: password,
                role: role
            })

        })
        .then(response => {

            if (!response.ok) {
                throw new Error("Registration failed");
            }

            return response.json();

        })
        .then(data => {

            if (!data) {
                message.textContent =
                    "Email already exists!";
                return;
            }

            message.textContent =
                "Account created successfully!";

            registerForm.reset();

        })
        .catch(error => {

            console.error(error);

            message.textContent =
                "Unable to create account.";

        });

    });

}


// ================= LOGIN =================

const loginForm = document.getElementById("loginForm");

if (loginForm) {

    loginForm.addEventListener("submit", function(event) {

        event.preventDefault();

        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        const message = document.getElementById("message");

        fetch(API_URL + "/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        })
        .then(response => response.json())
        .then(data => {

            if (data.token) {

                localStorage.setItem("token", data.token);
                localStorage.setItem("role", data.user.role);
                localStorage.setItem("userId", data.user.id);
                message.textContent = "Login successful!";
                window.location.href = "dashboard.html";

            } else {

                message.textContent = data.message;

            }

        })
        .catch(error => {

            console.error(error);

            message.textContent = "Unable to connect to server.";

        });

    });

}


// ================= DASHBOARD =================

const workspaceCount = document.getElementById("workspaceCount");

if (workspaceCount) {

    loadDashboard();

}


// Load dashboard data
function loadDashboard() {

    const token = localStorage.getItem("token");

    if (!token) {

        window.location.href = "login.html";
        return;

    }

    const headers = {
        "Authorization": "Bearer " + token
    };


    // Get workspaces
    fetch(API_URL + "/api/workspaces", {
        headers: headers
    })
    .then(response => {

        if (response.status === 401) {
            throw new Error("Unauthorized");
        }

        return response.json();

    })
    .then(workspaces => {

        document.getElementById("workspaceCount").textContent =
            workspaces.length;

    })
    .catch(error => {

        console.error("Workspace error:", error);

    });


    // Get projects
    fetch(API_URL + "/api/projects", {
        headers: headers
    })
    .then(response => {

        if (response.status === 401) {
            throw new Error("Unauthorized");
        }

        return response.json();

    })
    .then(projects => {

        document.getElementById("projectCount").textContent =
            projects.length;

    })
    .catch(error => {

        console.error("Project error:", error);

    });


    // Get tasks
    fetch(API_URL + "/api/tasks", {
        headers: headers
    })
    .then(response => {

        if (response.status === 401) {
            throw new Error("Unauthorized");
        }

        return response.json();

    })
    .then(tasks => {
        allTasks = tasks;

    document.getElementById("taskCount").textContent =
        tasks.length;

    const todoCount = tasks.filter(
        task => task.status === "TODO"
    ).length;

    const inProgressCount = tasks.filter(
        task => task.status === "IN PROGRESS"
    ).length;

    const doneCount = tasks.filter(
        task => task.status === "DONE"
    ).length;

    document.getElementById("todoCount").textContent =
        todoCount;

    document.getElementById("inProgressCount").textContent =
        inProgressCount;

    document.getElementById("doneCount").textContent =
        doneCount;

    document.getElementById("completedCount").textContent =
        doneCount;

    const today = new Date().toISOString().split("T")[0];

const overdue = tasks.filter(task =>
    task.dueDate &&
    task.dueDate < today &&
    task.status !== "DONE"
).length;

document.getElementById("overdueCount").textContent = overdue;
const overdueTasks = tasks.filter(task =>
    task.dueDate &&
    task.dueDate < today &&
    task.status !== "DONE"
);

const overdueTaskList =
    document.getElementById("overdueTaskList");

if (overdueTasks.length === 0) {

    overdueTaskList.innerHTML =
        "<p>No overdue tasks.</p>";

} else {

    overdueTaskList.innerHTML = "";

    overdueTasks.forEach(task => {

        const overdueItem =
            document.createElement("div");

        overdueItem.className = "overdue-task-card";

        overdueItem.innerHTML = `
            <h3>${task.title}</h3>

            <p>
                📅 Due: ${task.dueDate}
            </p>

            <p>
                👤 Assigned to:
                ${task.assignee
                    ? task.assignee.name
                    : "Not assigned"}
            </p>

            <p>
                🔴 ${task.status}
            </p>
        `;

        overdueTaskList.appendChild(overdueItem);
    });
}

    // Task Progress

const progressBar =
    document.getElementById("taskProgressBar");

const progressText =
    document.getElementById("taskProgressText");

if (tasks.length > 0) {

    const progress =
        Math.round((doneCount / tasks.length) * 100);

    progressBar.style.width = progress + "%";

    progressText.textContent =
        progress + "% Completed";

} else {

    progressBar.style.width = "0%";

    progressText.textContent =
        "0% Completed";
}
    displayTasks(allTasks);
})
    .catch(error => {

        console.error("Task error:", error);

        if (error.message === "Unauthorized") {
            localStorage.removeItem("token");
            window.location.href = "login.html";
        }

    });

}


// ================= DISPLAY TASKS =================

function displayTasks(tasks) {

    const taskList = document.getElementById("taskList");

    taskList.innerHTML = "";

    if (tasks.length === 0) {
        taskList.innerHTML = "<p>No tasks available.</p>";
        return;
    }

    tasks.forEach(task => {
        const today = new Date().toISOString().split("T")[0];

const isOverdue =
    task.dueDate &&
    task.dueDate < today &&
    task.status !== "DONE";

        const taskItem = document.createElement("div");

        taskItem.className =
    isOverdue ? "task-item overdue-task" : "task-item";

        taskItem.innerHTML = `
            <div class="task-header">

                <h3>${task.title}</h3>

                <span class="priority-badge ${task.priority.toLowerCase()}">
                    ${task.priority}
                </span>

            </div>

            <p class="task-description">
                ${task.description || "No description"}
            </p>

            <p class="task-date">
                📅 Due: ${task.dueDate || "Not set"}
            </p>
            <p class="task-assignee">
    👤 Assigned to:
    ${task.assignee
        ? task.assignee.name
        : "Not assigned"}
</p>
<p class="task-project">
    📁 Project:
    ${task.project
        ? task.project.name
        : "No project"}
</p>

            <div class="task-status">

                <label>
                    <strong>Status:</strong>
                </label>

                <select id="status-${task.id}">

                    <option value="TODO"
                        ${task.status === "TODO" ? "selected" : ""}>
                        TODO
                    </option>

                    <option value="IN PROGRESS"
                        ${task.status === "IN PROGRESS" ? "selected" : ""}>
                        IN PROGRESS
                    </option>

                    <option value="DONE"
                        ${task.status === "DONE" ? "selected" : ""}>
                        DONE
                    </option>

                </select>

                <button onclick="updateTaskStatus(${task.id})">
                    Update Status
                </button>

                <button
                    class="edit-button"
                    onclick="editTask(${task.id})">
                    Edit
                </button>

                ${
    localStorage.getItem("role") === "ADMIN" ||
    (
        task.createdBy &&
        Number(task.createdBy.id) ===
        Number(localStorage.getItem("userId"))
    )
    ? `
        <button
            class="delete-button"
            onclick="deleteTask(${task.id})">
            Delete
        </button>
    `
    : `
        <button
            class="delete-button disabled-delete-button"
            disabled
            title="You can only delete tasks created by you">
            Delete
        </button>
    `
}
                <button
                    class="comment-button"
                    onclick="openComments(${task.id})">
                    💬 Comments
                </button>

            </div>
        `;

        taskList.appendChild(taskItem);
    });
}

// ================= TASK FILTER =================

function filterTasks() {

    const searchText =
        document.getElementById("taskSearch").value.toLowerCase();

    const selectedStatus =
        document.getElementById("filterStatus").value;

    const selectedPriority =
        document.getElementById("filterPriority").value;

    const selectedProject =
    document.getElementById("filterProject").value;

    const selectedAssignee =
        document.getElementById("filterAssignee").value;

    const selectedSort =
        document.getElementById("sortTasks").value;

    const filteredTasks = allTasks.filter(task => {

        const matchesSearch =
            task.title.toLowerCase().includes(searchText) ||
            (task.description || "").toLowerCase().includes(searchText);

        const matchesStatus =
            !selectedStatus ||
            task.status === selectedStatus;

        const matchesPriority =
            !selectedPriority ||
            task.priority === selectedPriority;

        const matchesProject =
    !selectedProject ||
    (task.project && task.project.id == selectedProject);

        const matchesAssignee =
    !selectedAssignee ||
    (task.assignee && task.assignee.id == selectedAssignee);

        return matchesSearch &&
       matchesStatus &&
       matchesPriority &&
       matchesProject &&
       matchesAssignee;
    });

        // Sort tasks
    if (selectedSort === "title") {

        filteredTasks.sort((a, b) =>
            a.title.localeCompare(b.title)
        );

    } else if (selectedSort === "dueDate") {

        filteredTasks.sort((a, b) =>
            (a.dueDate || "").localeCompare(b.dueDate || "")
        );

    } else if (selectedSort === "priority") {

        const priorityOrder = {
            "HIGH": 1,
            "MEDIUM": 2,
            "LOW": 3
        };

        filteredTasks.sort((a, b) =>
            priorityOrder[a.priority] - priorityOrder[b.priority]
        );

    } else if (selectedSort === "status") {

        const statusOrder = {
            "TODO": 1,
            "IN PROGRESS": 2,
            "DONE": 3
        };

        filteredTasks.sort((a, b) =>
            statusOrder[a.status] - statusOrder[b.status]
        );

    } else if (selectedSort === "assignee") {

        filteredTasks.sort((a, b) => {

            const nameA = a.assignee?.name || "";
            const nameB = b.assignee?.name || "";

            return nameA.localeCompare(nameB);
        });
    }

    displayTasks(filteredTasks);
}


// ================= LOGOUT =================

const logoutButton = document.getElementById("logoutButton");

if (logoutButton) {

    logoutButton.addEventListener("click", function() {

        localStorage.removeItem("token");

        window.location.href = "login.html";

    });

}
// Create workspace

const createWorkspaceButton = document.getElementById("createWorkspaceButton");

if (createWorkspaceButton) {

    createWorkspaceButton.addEventListener("click", function () {

        const name = document.getElementById("workspaceName").value;
        const description = document.getElementById("workspaceDescription").value;

        const token = localStorage.getItem("token");

        fetch("http://localhost:8080/api/workspaces", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({
                name: name,
                description: description
            })
        })
        .then(response => response.json())
        .then(data => {

            console.log("Workspace created:", data);

            alert("Workspace created successfully!");

            loadWorkspaces();

        })
        .catch(error => {

            console.error(error);

            alert("Unable to create workspace.");

        });
    });
}


// Load workspaces

function loadWorkspaces() {

    const token = localStorage.getItem("token");

    fetch("http://localhost:8080/api/workspaces", {
        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(response => response.json())
    .then(data => {

        const workspaceList = document.getElementById("workspaceList");
        const workspaceCount = document.getElementById("workspaceCount");
        const projectWorkspace =
    document.getElementById("projectWorkspace");

        workspaceCount.textContent = data.length;

        if (data.length === 0) {
            workspaceList.innerHTML = "<p>No workspaces available.</p>";
            return;
        }

        workspaceList.innerHTML = "";

        if (projectWorkspace) {
    projectWorkspace.innerHTML =
        '<option value="">Select Workspace</option>';
}

        data.forEach(workspace => {

            const div = document.createElement("div");

            div.innerHTML = `
    <h3>${workspace.name}</h3>
    <p>${workspace.description}</p>

    <button onclick="editWorkspace(${workspace.id})">
        Edit
    </button>

    <button onclick="deleteWorkspace(${workspace.id})">
        Delete
    </button>
`;

            workspaceList.appendChild(div);

            if (projectWorkspace) {

    const option = document.createElement("option");

    option.value = workspace.id;
    option.textContent = workspace.name;

    projectWorkspace.appendChild(option);
}

        });

    })
    .catch(error => {
        console.error("Error loading workspaces:", error);
    });
}


// Load workspaces when dashboard opens

if (document.getElementById("workspaceList")) {
    loadWorkspaces();
}
// Create Project
const createProjectButton = document.getElementById("createProjectButton");

if (createProjectButton) {

    createProjectButton.addEventListener("click", function () {

        const name = document.getElementById("projectName").value;
        const description = document.getElementById("projectDescription").value;
        const workspaceId =
    document.getElementById("projectWorkspace").value;
        const token = localStorage.getItem("token");

        if (!name || !description || !workspaceId) {
    alert("Please enter project details and select a workspace.");
    return;
}

        fetch("http://localhost:8080/api/projects", {
            method: "POST",

            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },

            body: JSON.stringify({
    name: name,
    description: description,
    workspace: {
        id: Number(workspaceId)
    }
})
        })

        .then(response => response.json())

        .then(data => {

            console.log(data);

            alert("Project created successfully!");

            document.getElementById("projectName").value = "";
            document.getElementById("projectDescription").value = "";
            loadProjects();

        })

        .catch(error => {

            console.error(error);
            alert("Unable to create project.");

        });

    });

}
// Load projects

function loadProjects() {

    const token = localStorage.getItem("token");

    fetch("http://localhost:8080/api/projects", {
        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(response => response.json())
    .then(data => {

        const projectList = document.getElementById("projectList");
        const projectCount = document.getElementById("projectCount");
        const taskProject = document.getElementById("taskProject");
        const filterProject = document.getElementById("filterProject");

        projectCount.textContent = data.length;
        if (taskProject) {
    taskProject.innerHTML = '<option value="">Select Project</option>';
}
        if (filterProject) {
    filterProject.innerHTML = '<option value="">All Projects</option>';
}

        if (data.length === 0) {
            projectList.innerHTML = "<p>No projects available.</p>";
            return;
        }

        projectList.innerHTML = "";

        data.forEach(project => {

    const div = document.createElement("div");

    div.innerHTML = `
    <h3>${project.name}</h3>
    <p>${project.description}</p>

    <button onclick="editProject(${project.id})">
        Edit
    </button>

    <button onclick="deleteProject(${project.id})">
        Delete
    </button>
`;

    projectList.appendChild(div);

    if (taskProject) {
        const option = document.createElement("option");
        option.value = project.id;
        option.textContent = project.name;
        taskProject.appendChild(option);
    }
    if (filterProject) {
    const option = document.createElement("option");
    option.value = project.id;
    option.textContent = project.name;
    filterProject.appendChild(option);
}

});

    })
    .catch(error => {
        console.error("Error loading projects:", error);
    });
}
// Load projects when dashboard opens

if (document.getElementById("projectList")) {
    loadProjects();
}
const createTaskButton =
    document.getElementById("createTaskButton");

if (createTaskButton) {

    createTaskButton.addEventListener("click", function () {

        const title =
            document.getElementById("taskTitle").value;

        const description =
            document.getElementById("taskDescription").value;

        const priority =
            document.getElementById("taskPriority").value;

            const status = document.getElementById("taskStatus").value;
            

        const dueDate =
            document.getElementById("taskDueDate").value;

        const token =
            localStorage.getItem("token");

        const projectId =
    document.getElementById("taskProject").value;
        const assigneeId =
    userRole === "ADMIN"
        ? document.getElementById("taskAssignee").value
        : userId;

if (!title || !description || !priority || !dueDate || !projectId || !assigneeId) {
    alert("Please enter all task details.");
    return;
}

        fetch("http://localhost:8080/api/tasks", {

            method: "POST",

            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },

            body: JSON.stringify({

                title: title,
                description: description,
                status: status,
                priority: priority,
                dueDate: dueDate,

                project: {
    id: Number(projectId)
},
                assignee: {
    id: Number(assigneeId)
}

            })

        })
        .then(response => response.json())

        .then(data => {

            console.log(data);

            alert("Task created successfully!");

            document.getElementById("taskTitle").value = "";
            document.getElementById("taskDescription").value = "";
            document.getElementById("taskPriority").value = "";
            document.getElementById("taskDueDate").value = "";

            loadDashboard();

        })

        .catch(error => {

            console.error(error);

            alert("Unable to create task.");

        });

    });

}
// ================= UPDATE TASK STATUS =================

function updateTaskStatus(taskId) {

    const status = document.getElementById(
        "status-" + taskId
    ).value;

    const token = localStorage.getItem("token");

    fetch(
        API_URL +
        "/api/tasks/" +
        taskId +
        "/status?status=" +
        encodeURIComponent(status),
        {
            method: "PUT",

            headers: {
                "Authorization": "Bearer " + token
            }
        }
    )
    .then(response => {

        if (!response.ok) {
            throw new Error("Unable to update task status");
        }

        return response.json();
    })
    .then(data => {

        alert("Task status updated successfully!");

        loadDashboard();
    })
    .catch(error => {

        console.error("Status update error:", error);

        alert("Unable to update task status.");
    });
}

// ================= EDIT TASK =================

function editTask(taskId) {

    const token = localStorage.getItem("token");

    fetch(API_URL + "/api/tasks/" + taskId, {

        method: "GET",

        headers: {
            "Authorization": "Bearer " + token
        }

    })
    .then(response => {

        if (!response.ok) {
            throw new Error("Unable to load task");
        }

        return response.json();

    })
    .then(task => {

        document.getElementById("editTaskId").value =
            task.id;

        document.getElementById("editTaskTitle").value =
            task.title || "";

        document.getElementById("editTaskDescription").value =
            task.description || "";

        document.getElementById("editTaskStatus").value =
            task.status || "TODO";

        document.getElementById("editTaskPriority").value =
            task.priority || "MEDIUM";
        
        document.getElementById("editTaskAssignee").value =
            task.assignee ? task.assignee.id : "";

        document.getElementById("editTaskDueDate").value =
            task.dueDate || "";

        document.getElementById("editTaskModal").style.display =
            "flex";

    })
    .catch(error => {

        console.error("Edit task error:", error);

        alert("Unable to load task.");

    });
}


// ================= CLOSE EDIT MODAL =================

function closeEditModal() {

    document.getElementById("editTaskModal").style.display =
        "none";
}
// ================= SAVE EDITED TASK =================

document.getElementById("editTaskForm").addEventListener(
    "submit",
    function(event) {

        event.preventDefault();

        const taskId =
            document.getElementById("editTaskId").value;

       const updatedTask = {

    title: document.getElementById("editTaskTitle").value,

    description: document.getElementById("editTaskDescription").value,

    status: document.getElementById("editTaskStatus").value,

    priority: document.getElementById("editTaskPriority").value,

    dueDate: document.getElementById("editTaskDueDate").value
};

if (userRole === "ADMIN") {

    updatedTask.assignee = {
        id: document.getElementById("editTaskAssignee").value
    };

}

        const token = localStorage.getItem("token");

        fetch(API_URL + "/api/tasks/" + taskId, {

            method: "PUT",

            headers: {

                "Content-Type": "application/json",

                "Authorization": "Bearer " + token
            },

            body: JSON.stringify(updatedTask)

        })
        .then(response => {

            if (!response.ok) {
                throw new Error("Unable to update task");
            }

            return response.json();

        })
        .then(data => {

            alert("Task updated successfully!");

            closeEditModal();

            loadDashboard();

        })
        .catch(error => {

            console.error("Update task error:", error);

            alert("Unable to update task.");

        });

    }
);

// ================= DELETE TASK =================

function deleteTask(taskId) {

    document.getElementById("deleteTaskId").value = taskId;

    document.getElementById("deleteTaskModal").style.display = "flex";
}


// ================= CONFIRM DELETE =================

function confirmDeleteTask() {

    const taskId =
        document.getElementById("deleteTaskId").value;

    const token = localStorage.getItem("token");

    fetch(API_URL + "/api/tasks/" + taskId, {

        method: "DELETE",

        headers: {
            "Authorization": "Bearer " + token
        }

    })
    .then(response => {

        if (!response.ok) {
            throw new Error("Unable to delete task");
        }

        return response.text();

    })
    .then(() => {

        alert("Task deleted successfully!");

        closeDeleteModal();

        loadDashboard();

    })
    .catch(error => {

        console.error("Delete task error:", error);

        alert("Unable to delete task.");

    });
}


// ================= CLOSE DELETE MODAL =================

function closeDeleteModal() {

    document.getElementById("deleteTaskModal").style.display =
        "none";
}
// ================= LOAD USERS =================

function loadUsers() {

    const token = localStorage.getItem("token");

    fetch(API_URL + "/api/users", {
        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(response => {

        if (!response.ok) {
            throw new Error("Unable to load users");
        }

        return response.json();
    })
    .then(users => {

        console.log("Users loaded:", users);

        // Create Task dropdown
        const taskAssignee = document.getElementById("taskAssignee");

        // Edit Task dropdown
        const editTaskAssignee = document.getElementById("editTaskAssignee");

        // Filter Assignee dropdown
        const filterAssignee = document.getElementById("filterAssignee");

        users.forEach(user => {

            if (taskAssignee) {
                const option = document.createElement("option");

                option.value = user.id;
                option.textContent = user.name + " (" + user.email + ")";

                taskAssignee.appendChild(option);
            }

            if (editTaskAssignee) {
                const option = document.createElement("option");

                option.value = user.id;
                option.textContent = user.name + " (" + user.email + ")";

                editTaskAssignee.appendChild(option);
            }

            if (filterAssignee) {
                const option = document.createElement("option");

                option.value = user.id;
                option.textContent = user.name + " (" + user.email + ")";

                filterAssignee.appendChild(option);
            }

        });

    })
    .catch(error => {

        console.error("User loading error:", error);

    });
}


// Load users when dashboard opens

if (document.getElementById("taskAssignee")) {
    loadUsers();
}
// Search and filter events

const taskSearch =
    document.getElementById("taskSearch");

const filterStatus =
    document.getElementById("filterStatus");

const filterPriority =
    document.getElementById("filterPriority");

const filterAssignee =
    document.getElementById("filterAssignee");

const filterProject =
    document.getElementById("filterProject");

const sortTasks =
    document.getElementById("sortTasks");


if (taskSearch) {
    taskSearch.addEventListener("input", filterTasks);
}

if (filterStatus) {
    filterStatus.addEventListener("change", filterTasks);
}

if (filterPriority) {
    filterPriority.addEventListener("change", filterTasks);
}

if (filterAssignee) {
    filterAssignee.addEventListener("change", filterTasks);
}

if (filterProject) {
    filterProject.addEventListener("change", filterTasks);
}

if (sortTasks) {
    sortTasks.addEventListener("change", filterTasks);
}
function editProject(projectId) {

    const newName = prompt("Enter new project name:");

    if (!newName) {
        return;
    }

    const newDescription = prompt("Enter new project description:");

    if (!newDescription) {
        return;
    }

    const token = localStorage.getItem("token");

    fetch(API_URL + "/api/projects/" + projectId, {
        method: "PUT",

        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },

        body: JSON.stringify({
            name: newName,
            description: newDescription
        })
    })
    .then(async response => {

    if (!response.ok) {

        const message = await response.text();

        throw new Error(message);
    }

    return response.json();
})
    .then(data => {

        alert("Project updated successfully!");

        loadProjects();
        loadDashboard();
    })
    .catch(error => {

    console.error(error);
    alert(error.message);

});
}
function deleteProject(projectId) {

    const confirmDelete =
        confirm("Are you sure you want to delete this project?");

    if (!confirmDelete) {
        return;
    }

    const token = localStorage.getItem("token");

    fetch(API_URL + "/api/projects/" + projectId, {
        method: "DELETE",

        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(async response => {

    if (!response.ok) {

        const message = await response.text();

        throw new Error(message);
    }

    return response.text();
})
    .then(data => {

        alert("Project deleted successfully!");

        loadProjects();
        loadDashboard();
    })
    .catch(error => {

    console.error(error);
    alert(error.message);
});
}
function editWorkspace(workspaceId) {

    const newName = prompt("Enter new workspace name:");

    if (!newName) {
        return;
    }

    const newDescription =
        prompt("Enter new workspace description:");

    if (!newDescription) {
        return;
    }

    const token = localStorage.getItem("token");

    fetch(API_URL + "/api/workspaces/" + workspaceId, {
        method: "PUT",

        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },

        body: JSON.stringify({
            name: newName,
            description: newDescription
        })
    })
    .then(response => {

        if (!response.ok) {
            throw new Error("Unable to update workspace");
        }

        return response.json();
    })
    .then(data => {

        alert("Workspace updated successfully!");

        loadWorkspaces();
        loadDashboard();
    })
    .catch(error => {

        console.error(error);
        alert("Unable to update workspace.");
    });
}
function deleteWorkspace(workspaceId) {

    const confirmDelete =
        confirm("Are you sure you want to delete this workspace?");

    if (!confirmDelete) {
        return;
    }

    const token = localStorage.getItem("token");

    fetch(API_URL + "/api/workspaces/" + workspaceId, {
        method: "DELETE",

        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(response => {

        if (!response.ok) {
            throw new Error("Unable to delete workspace");
        }

        return response.text();
    })
    .then(data => {

        alert("Workspace deleted successfully!");

        loadWorkspaces();
        loadDashboard();
    })
    .catch(error => {

        console.error(error);
        alert("Unable to delete workspace.");
    });
}
// ================= SIDEBAR TOGGLE =================

const sidebarToggle = document.getElementById("sidebarToggle");
const sidebar = document.querySelector(".sidebar");

if (sidebarToggle && sidebar) {

    sidebarToggle.addEventListener("click", function () {

        sidebar.classList.toggle("collapsed");
        document.body.classList.toggle("sidebar-closed");

    });

}

// Safely display user-entered text
function escapeHTML(text) {
    const div = document.createElement("div");
    div.textContent = text || "";
    return div.innerHTML;
}

// ================= TASK COMMENTS =================

let currentCommentTaskId = null;


// Open comments popup
function openComments(taskId) {

    currentCommentTaskId = taskId;

    const modal = document.getElementById("commentsModal");

    modal.style.display = "flex";

    loadComments(taskId);
}


// Close comments popup
function closeComments() {

    const modal = document.getElementById("commentsModal");

    modal.style.display = "none";

    currentCommentTaskId = null;
}

// Format comment date and time
function formatCommentDate(dateString) {

    if (!dateString) {
        return "";
    }

    const date = new Date(dateString);

    return date.toLocaleString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "numeric",
        minute: "2-digit",
        hour12: true
    });
}

// Load comments
function loadComments(taskId) {

    const currentUserId = Number(
    localStorage.getItem("userId")
    );

    const currentUserRole =
    localStorage.getItem("role");

    const token = localStorage.getItem("token");

    const commentsList = document.getElementById("commentsList");

    commentsList.innerHTML = "<p>Loading comments...</p>";

    fetch(
        "http://localhost:8080/api/tasks/" + taskId + "/comments",
        {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + token
            }
        }
    )
    .then(response => response.json())
    .then(data => {
    commentsList.innerHTML = "";

    const currentUserId =
        Number(localStorage.getItem("userId"));

    const currentRole =
        localStorage.getItem("role");

    if (!Array.isArray(data) || data.length === 0) {

            commentsList.innerHTML =
                "<p>No comments yet.</p>";

            return;
        }

        data.forEach(comment => {

            const div = document.createElement("div");

            div.className = "comment-item";
            const safeComment =
                escapeHTML(comment.comment);

            const safeUserName =
                escapeHTML(comment.userName);

            div.innerHTML = `
                <div class="comment-user">
                    👤 ${safeUserName || "Unknown User"}
                </div>

                <div class="comment-text">
                    ${safeComment}
                </div>

                <div class="comment-time">
                    🕒 ${formatCommentDate(comment.createdAt)}
                </div>

                <div class="comment-actions">

                    ${
                        Number(comment.userId) === currentUserId
                        ? `
                            <button
                                class="edit-comment-button"
                                onclick="editComment(${comment.id})">
                                Edit
                             </button>
                        `
                        : ""
                    }

                    ${
                        Number(comment.userId) === currentUserId ||
                        currentUserRole === "ADMIN"
                        ? `
                            <button
                                class="delete-comment-button"
                                onclick="deleteComment(${comment.id})">
                                Delete
                            </button>
                        `
                        : ""
                    }   

                </div>
            `;

            commentsList.appendChild(div);

        });

    })
    .catch(error => {

        console.error(
            "Error loading comments:",
            error
        );

        commentsList.innerHTML =
            "<p>Unable to load comments.</p>";

    });
}
// Add a new comment
function addComment() {

    const commentInput =
        document.getElementById("commentInput");

    const commentText =
        commentInput.value.trim();

    if (commentText === "") {

        alert("Please enter a comment.");

        return;
    }

    const token =
        localStorage.getItem("token");

    if (!currentCommentTaskId) {

        alert("Task not selected.");

        return;
    }

    fetch(
        "http://localhost:8080/api/tasks/" +
        currentCommentTaskId +
        "/comments",
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },

            body: JSON.stringify({
                comment: commentText
            })
        }
    )
    .then(response => {

        if (!response.ok) {
            throw new Error("Failed to add comment");
        }

        return response.json();
    })
    .then(data => {

        commentInput.value = "";

        loadComments(currentCommentTaskId);

    })
    .catch(error => {

        console.error(
            "Error adding comment:",
            error
        );

        alert("Unable to add comment.");
    });
}
// Delete a comment
function deleteComment(commentId) {

    const token = localStorage.getItem("token");

    if (!confirm("Are you sure you want to delete this comment?")) {
        return;
    }

    fetch(
        "http://localhost:8080/api/comments/" + commentId,
        {
            method: "DELETE",

            headers: {
                "Authorization": "Bearer " + token
            }
        }
    )
    .then(response => {

        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(
                    data.message || "Unable to delete comment"
                );
            });
        }

        return response.json();
    })
    .then(data => {

        alert("Comment deleted successfully.");

        loadComments(currentCommentTaskId);

    })
    .catch(error => {

        console.error(
            "Error deleting comment:",
            error
        );

        alert(error.message);
    });
}
// Edit a comment
function editComment(commentId) {

    const token = localStorage.getItem("token");

    if (!currentCommentTaskId) {
        alert("Task not selected.");
        return;
    }

    fetch(
        "http://localhost:8080/api/tasks/" +
        currentCommentTaskId +
        "/comments",
        {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + token
            }
        }
    )
    .then(response => {

        if (!response.ok) {
            throw new Error("Unable to load comment.");
        }

        return response.json();
    })
    .then(comments => {

        const comment = comments.find(
            item => item.id === commentId
        );

        if (!comment) {
            alert("Comment not found.");
            return;
        }

        const newComment = prompt(
            "Edit your comment:",
            comment.comment
        );

        if (newComment === null) {
            return;
        }

        if (newComment.trim() === "") {
            alert("Comment cannot be empty.");
            return;
        }

        return fetch(
            "http://localhost:8080/api/comments/" +
            commentId,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    comment: newComment.trim()
                })
            }
        );
    })
    .then(response => {

        if (!response) {
            return;
        }

        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(
                    data.message ||
                    "Unable to edit comment"
                );
            });
        }

        return response.json();
    })
    .then(data => {

        if (!data) {
            return;
        }

        alert("Comment updated successfully.");

        loadComments(currentCommentTaskId);
    })
    .catch(error => {

        console.error(
            "Error editing comment:",
            error
        );

        alert(error.message);
    });
}


/* ================= NOTIFICATIONS ================= */

const notificationButton =
    document.getElementById("notificationButton");

const notificationDropdown =
    document.getElementById("notificationDropdown");

const notificationBadge =
    document.getElementById("notificationBadge");

const notificationList =
    document.getElementById("notificationList");

const refreshNotificationsButton =
    document.getElementById("refreshNotificationsButton");

// Check whether a notification has been read
function isNotificationRead(notification) {
    return notification.read === true ||
           notification.isRead === true;
}

// Update unread badge
function loadUnreadNotificationCount() {
    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");

    if (!token || !currentUserId || !notificationBadge) {
        return;
    }

    fetch(
        API_URL + "/api/notifications/user/" +
        encodeURIComponent(currentUserId) +
        "/unread-count",
        {
            headers: {
                "Authorization": "Bearer " + token
            }
        }
    )
    .then(response => {
        if (!response.ok) {
            throw new Error("Unable to load unread count");
        }

        return response.json();
    })
    .then(data => {
        const count = Number(data.unreadCount) || 0;

        notificationBadge.textContent =
            count > 99 ? "99+" : String(count);

        notificationBadge.dataset.count = String(count);
        notificationBadge.style.display =
            count > 0 ? "inline-block" : "none";
    })
    .catch(error => {
        console.error("Notification count error:", error);
    });
}

// Load notification list
function loadNotifications() {
    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");

    if (!token || !currentUserId || !notificationList) {
        return;
    }

    notificationList.innerHTML =
        '<p class="notification-empty">Loading notifications...</p>';

    fetch(
        API_URL + "/api/notifications/user/" +
        encodeURIComponent(currentUserId),
        {
            headers: {
                "Authorization": "Bearer " + token
            }
        }
    )
    .then(response => {
        if (!response.ok) {
            throw new Error("Unable to load notifications");
        }

        return response.json();
    })
    .then(notifications => {
        notificationList.innerHTML = "";

        if (!Array.isArray(notifications) ||
            notifications.length === 0) {

            notificationList.innerHTML =
                '<p class="notification-empty">No notifications yet.</p>';

            loadUnreadNotificationCount();
            return;
        }

        notifications.forEach(notification => {
            const item = document.createElement("button");

            item.type = "button";
            item.className = "notification-item";

            const alreadyRead = isNotificationRead(notification);

            if (!alreadyRead) {
                item.classList.add("unread");
            }

            const message = document.createElement("span");
            message.className = "notification-message";
            message.textContent =
                notification.message || "Notification";

            const date = document.createElement("span");
            date.className = "notification-date";

            if (notification.createdAt) {
                const createdDate =
                    new Date(notification.createdAt);

                date.textContent =
                    Number.isNaN(createdDate.getTime())
                        ? notification.createdAt
                        : createdDate.toLocaleString("en-IN");
            } else {
                date.textContent = "";
            }

            item.appendChild(message);
            item.appendChild(date);

            item.addEventListener("click", function () {
                markNotificationAsRead(notification.id);
            });

            notificationList.appendChild(item);
        });

        loadUnreadNotificationCount();
    })
    .catch(error => {
        console.error("Notification loading error:", error);

        notificationList.innerHTML =
            '<p class="notification-empty">Unable to load notifications. Please refresh.</p>';
    });
}

// Mark a notification as read
function markNotificationAsRead(notificationId) {
    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");

    if (!token || !currentUserId) {
        return;
    }

    fetch(
        API_URL + "/api/notifications/" +
        encodeURIComponent(notificationId) +
        "/read?userId=" +
        encodeURIComponent(currentUserId),
        {
            method: "PUT",
            headers: {
                "Authorization": "Bearer " + token
            }
        }
    )
    .then(response => {
        if (!response.ok) {
            throw new Error("Unable to mark notification as read");
        }

        loadNotifications();
        loadUnreadNotificationCount();
    })
    .catch(error => {
        console.error("Mark notification as read error:", error);
        alert("Unable to update notification. Please try again.");
    });
}

// Open and close notification dropdown
if (notificationButton && notificationDropdown) {
    notificationButton.addEventListener("click", function () {
        const isOpening = notificationDropdown.hidden;

        notificationDropdown.hidden = !isOpening;

        notificationButton.setAttribute(
            "aria-expanded",
            String(isOpening)
        );

        if (isOpening) {
            loadNotifications();
        }
    });
}

// Refresh button
if (refreshNotificationsButton) {
    refreshNotificationsButton.addEventListener("click", function () {
        loadNotifications();
        loadUnreadNotificationCount();
    });
}

// Load unread count when the dashboard opens
if (notificationButton) {
    loadUnreadNotificationCount();
}
