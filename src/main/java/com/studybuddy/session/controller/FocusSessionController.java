package com.studybuddy.session.controller;

import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import com.studybuddy.session.dto.SessionStatisticsResponse;
import com.studybuddy.session.dto.StartSessionRequest;
import com.studybuddy.session.model.FocusSession;
import com.studybuddy.session.repository.FocusSessionRepository;
import com.studybuddy.task.model.Task;
import com.studybuddy.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// FocusSessionController manages the lifecycle of study sessions and user performance metrics.
// It implements the core functionality for "Focus Sessions" as described in Section 3.2.3.
// This controller also handles the gamification logic (FS-6) by calculating user ratings.
@RestController
@RequestMapping("/api/session")
public class FocusSessionController {

    // Repository for persisting and retrieving study session records.
    @Autowired
    private FocusSessionRepository sessionRepository;

    // Repository used to update user profiles with newly calculated ratings.
    @Autowired
    private UserRepository userRepository;

    // Repository for managing tasks associated with specific focus sessions.
    @Autowired
    private TaskRepository taskRepository;

    // Initializes a new focus session when a user starts studying (UI-3.1).
    // This creates the session container that will later be used to track tasks and duration.
    @PostMapping("/start")
    public ResponseEntity<?> startSession(@AuthenticationPrincipal User user,
                                          @RequestBody StartSessionRequest request) {
        FocusSession session = new FocusSession();
        // Link the session to the authenticated user's ID for history tracking.
        session.setUserId(user.getId());
        // Record the intended duration selected during the matching phase.
        session.setDurationMinutes(request.getDurationMinutes());
        // Store the real-time channel name for session context synchronization.
        session.setChannelName(request.getChannelName());

        FocusSession saved = sessionRepository.save(session);
        // Returns the unique session ID to the client for subsequent update/complete calls.
        return ResponseEntity.ok(Map.of("sessionId", saved.getId()));
    }

    // Marks a session as successfully finished when the timer expires.
    // This triggers the positive rating recalculation logic specified in Section 3.2.3.
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeSession(@AuthenticationPrincipal User user,
                                             @PathVariable Long id) {
        Optional<FocusSession> opt = sessionRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("Session not found");

        FocusSession session = opt.get();
        // Security check: Ensure users can only modify their own session data.
        if (!session.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not your session");
        }

        // Set status to completed and record the final end time.
        session.setCompleted(true);
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);

        // FS-6: Update the user's platform rating based on their new completion ratio.
        recalculateRating(user.getId());
        return ResponseEntity.ok("Session marked as completed");
    }

    // Marks a session as incomplete, typically if a user leaves early.
    // This provides the "Leaving Early" feedback loop mentioned in the requirements.
    @PutMapping("/{id}/incomplete")
    public ResponseEntity<?> incompleteSession(@AuthenticationPrincipal User user,
                                               @PathVariable Long id) {
        Optional<FocusSession> opt = sessionRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("Session not found");

        FocusSession session = opt.get();
        if (!session.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not your session");
        }

        // Explicitly mark as failed to differentiate from active or successful sessions.
        session.setCompleted(false);
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);

        // Recalculate rating: This may lead to a decrease in the user's overall reliability score.
        recalculateRating(user.getId());
        return ResponseEntity.ok("Session marked as incomplete");
    }

    // Retrieves the user's study history, including associated tasks for each session.
    // This supports the "History" view in the user profile dashboard.
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@AuthenticationPrincipal User user) {
        // Fetch sessions in descending order to show the most recent study activity first.
        List<FocusSession> sessions = sessionRepository
                .findByUserIdOrderByStartedAtDesc(user.getId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (FocusSession s : sessions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("durationMinutes", s.getDurationMinutes());
            item.put("completed", s.getCompleted());
            item.put("channelName", s.getChannelName());
            item.put("startedAt", s.getStartedAt() != null ? s.getStartedAt().toString() : null);
            item.put("endedAt", s.getEndedAt() != null ? s.getEndedAt().toString() : null);

            // Fetch and map tasks that were successfully completed during this specific session.
            List<Task> tasks = taskRepository.findBySessionIdAndCompletedTrueOrderByCreatedAtAsc(s.getId());
            List<Map<String, Object>> taskList = tasks.stream().map(t -> {
                Map<String, Object> tm = new LinkedHashMap<>();
                tm.put("id", t.getId());
                tm.put("title", t.getTitle());
                tm.put("note", t.getNote());
                tm.put("completed", t.getCompleted());
                return tm;
            }).collect(Collectors.toList());
            item.put("tasks", taskList);

            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    // Aggregates study data to provide high-level metrics for the user dashboard.
    // Calculates total time spent studying and the current reliability rating.
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(@AuthenticationPrincipal User user) {
        long total = sessionRepository.countByUserId(user.getId());
        long completed = sessionRepository.countByUserIdAndCompleted(user.getId(), true);

        // Calculate a 5-star rating based on the session completion percentage.
        double rating = (total == 0) ? 0.0 : ((double) completed / total) * 5.0;
        rating = Math.round(rating * 10.0) / 10.0;

        // Sum up the total minutes from all successful focus sessions.
        List<FocusSession> sessions = sessionRepository
                .findByUserIdOrderByStartedAtDesc(user.getId());
        long totalMinutes = 0;
        for (FocusSession s : sessions) {
            if (Boolean.TRUE.equals(s.getCompleted())) {
                totalMinutes += s.getDurationMinutes();
            }
        }

        // Return a summarized DTO for easy consumption by the frontend statistics charts.
        return ResponseEntity.ok(
                new SessionStatisticsResponse(rating, totalMinutes, completed, total));
    }

    // FS-6 Internal Logic: rating = (completedSessions / totalSessions) * 5.0.
    // This private helper ensures the user's profile is always synchronized with their session history.
    private void recalculateRating(Long userId) {
        long total = sessionRepository.countByUserId(userId);
        long completed = sessionRepository.countByUserIdAndCompleted(userId, true);

        // Perform the calculation and round to one decimal place for display purposes.
        double raw = (total == 0) ? 0.0 : ((double) completed / total) * 5.0;
        double finalRating = Math.round(raw * 10.0) / 10.0;

        // Persist the new rating directly to the User model.
        userRepository.findById(userId).ifPresent(u -> {
            u.setRating((float) finalRating);
            userRepository.save(u);
        });
    }
}