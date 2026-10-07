package com.studybuddy.task.controller;

import com.studybuddy.account.model.User;
import com.studybuddy.session.model.FocusSession;
import com.studybuddy.session.repository.FocusSessionRepository;
import com.studybuddy.task.dto.CreateTaskRequest;
import com.studybuddy.task.model.Task;
import com.studybuddy.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskController serves as the API gateway for all mission-oriented actions.
 * It strictly adheres to the "Focus Session" business logic defined in Section 3.2.3.
 * * Business Purpose:
 * 1. Maintain a persistent 'To-Do' list for students.
 * 2. Bridge the gap between static tasks and active, timed study sessions.
 * 3. Provide granular data for the User History Dashboard (UI-3.2).
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    // Handles the low-level data persistence for individual task items.
    @Autowired
    private TaskRepository taskRepository;

    // Provides access to session metadata to facilitate automatic task-session pairing.
    @Autowired
    private FocusSessionRepository focusSessionRepository;

    /**
     * Endpoint: POST /api/tasks
     * Action: Creates a standalone task or a task within an active study context.
     * * Implementation Details:
     * - Trims input data to prevent database bloat and UI alignment issues.
     * - Uses @AuthenticationPrincipal to guarantee that task ownership is tied to the caller.
     */
    @PostMapping
    public ResponseEntity<?> createTask(@AuthenticationPrincipal User user,
                                        @RequestBody CreateTaskRequest request) {
        // Validation: Prevents empty entries which could clutter the UI-3.1 task list.
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Task title is required");
        }

        // Object Mapping: Manually mapping DTO to Entity for precision control.
        Task task = new Task();
        task.setUserId(user.getId());
        task.setTitle(request.getTitle().trim());
        // Defaulting to null if no note is provided ensures database cleanliness.
        task.setNote(request.getNote() != null ? request.getNote().trim() : null);

        Task saved = taskRepository.save(task);
        // Returns the fresh record to the frontend to allow for immediate ID assignment in the view.
        return ResponseEntity.ok(toMap(saved));
    }

    /**
     * Endpoint: GET /api/tasks
     * Action: Fetches the user's entire task inventory.
     * * User Experience:
     * - Sorting by 'createdAt' in descending order ensures the user sees their newest goals first.
     */
    @GetMapping
    public ResponseEntity<?> getAllTasks(@AuthenticationPrincipal User user) {
        List<Task> tasks = taskRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(toMaps(tasks));
    }

    /**
     * Endpoint: GET /api/tasks/session/{sessionId}
     * Action: Filters tasks that were active during a specific historical interval.
     * * Logical Alignment:
     * - This endpoint is crucial for the 'Session Summary' feature where users recap their efficiency.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getTasksForSession(@AuthenticationPrincipal User user,
                                                @PathVariable Long sessionId) {
        // Order by Ascending ensures the user sees their task completion order as it happened.
        List<Task> tasks = taskRepository
                .findByUserIdAndSessionIdOrderByCreatedAtAsc(user.getId(), sessionId);
        return ResponseEntity.ok(toMaps(tasks));
    }

    /**
     * Endpoint: PUT /api/tasks/{id}/complete
     * Action: Transitions a task state to 'Completed'.
     * * Auto-Link Intelligence:
     * - Per Section 3.2.3, tasks checked during a session should be linked to that session.
     * - This logic finds the most recent session record so the user doesn't have to manually link them.
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> markComplete(@AuthenticationPrincipal User user,
                                          @PathVariable Long id) {
        Optional<Task> opt = taskRepository.findById(id);
        // Error Handling: Standard 404-style response for missing resources.
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("Task not found");

        Task task = opt.get();
        // Permission Check: Critical security measure to prevent cross-account task manipulation.
        if (!task.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not your task");
        }

        task.setCompleted(true);

        // Session Binding: Attaches the task to the current 'active' study window if one exists.
        List<FocusSession> sessions = focusSessionRepository
                .findByUserIdOrderByStartedAtDesc(user.getId());
        if (!sessions.isEmpty()) {
            task.setSessionId(sessions.get(0).getId());
        }

        taskRepository.save(task);
        return ResponseEntity.ok("Task marked as complete");
    }

    /**
     * Endpoint: PUT /api/tasks/{id}/incomplete
     * Action: Reverts a task to 'Active' status.
     * * Business Logic:
     * - If a task is unchecked, it loses its session context to allow for re-assignment later.
     */
    @PutMapping("/{id}/incomplete")
    public ResponseEntity<?> markIncomplete(@AuthenticationPrincipal User user,
                                            @PathVariable Long id) {
        Optional<Task> opt = taskRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("Task not found");

        Task task = opt.get();
        if (!task.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not your task");
        }

        task.setCompleted(false);
        // Resetting the sessionId ensures the task appears in the 'To-Do' pool again.
        task.setSessionId(null);
        taskRepository.save(task);
        return ResponseEntity.ok("Task unchecked");
    }

    /**
     * Endpoint: DELETE /api/tasks/{id}
     * Action: Permanently removes a task record.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@AuthenticationPrincipal User user,
                                        @PathVariable Long id) {
        Optional<Task> opt = taskRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("Task not found");

        Task task = opt.get();
        if (!task.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not your task");
        }

        // This action is irreversible to maintain data consistency.
        taskRepository.delete(task);
        return ResponseEntity.ok("Task deleted");
    }

    /**
     * Utility Mapping Method:
     * Converts an internal Task Entity into a serialized Map.
     * Uses LinkedHashMap to maintain a consistent JSON key order for frontend predictability.
     */
    private Map<String, Object> toMap(Task t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("sessionId", t.getSessionId());
        m.put("title", t.getTitle());
        m.put("note", t.getNote());
        m.put("completed", t.getCompleted());
        // String conversion of LocalDateTime ensures compatibility with the mobile client's date parser.
        m.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
        return m;
    }

    /**
     * Collection Utility:
     * Streamlines the conversion of lists, maintaining efficiency for larger history sets.
     */
    private List<Map<String, Object>> toMaps(List<Task> tasks) {
        return tasks.stream().map(this::toMap).collect(Collectors.toList());
    }
}