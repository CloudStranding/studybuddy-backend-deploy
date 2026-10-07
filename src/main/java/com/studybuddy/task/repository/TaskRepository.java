package com.studybuddy.task.repository;

import com.studybuddy.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// TaskRepository provides the data access abstraction for User Tasks within the Study Buddy ecosystem.
// It supports the functional requirements for task management and historical reporting.
// This interface allows the system to filter tasks based on user ownership and study session context.
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Retrieves all tasks associated with a specific user, ordered by creation date (newest first).
    // This supports the global task list view in the User Profile or Dashboard.
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Finds tasks that have been created but are not yet linked to any specific study session.
    // This allows users to view their "To-Do" pool before or during a new Focus Session.
    List<Task> findByUserIdAndSessionIdIsNullOrderByCreatedAtDesc(Long userId);

    // Retrieves tasks linked to a specific focus session for a particular user.
    // Used during the active Focus Session (UI-3.1) to display the current session's objectives.
    List<Task> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, Long sessionId);

    // Filters and returns only the successfully completed tasks for a given session.
    // This is utilized by the history and statistics modules to highlight achievements.
    List<Task> findBySessionIdAndCompletedTrueOrderByCreatedAtAsc(Long sessionId);

    // Retrieves all tasks linked to a session, regardless of their completion status.
    // Essential for the "History Details" view where a user reviews all planned vs. finished items.
    List<Task> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}