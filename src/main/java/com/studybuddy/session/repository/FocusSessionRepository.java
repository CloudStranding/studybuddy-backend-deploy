package com.studybuddy.session.repository;

import com.studybuddy.session.model.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// FocusSessionRepository provides the data access abstraction for the FocusSession entity.
// It supports the functional requirements for tracking user productivity and study history.
// This interface is critical for the gamification engine to calculate user reliability ratings.
@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    // Retrieves all focus sessions for a specific user, ordered by the start time in descending order.
    // This supports the "Study History" UI-3.2 where users can review their past performance.
    List<FocusSession> findByUserIdOrderByStartedAtDesc(Long userId);

    // Counts the total number of sessions initiated by a specific user.
    // This value serves as the denominator for the completion rate calculation defined in FS-6.
    long countByUserId(Long userId);

    // Counts the number of sessions a user successfully finished based on the 'completed' flag.
    // This value is the numerator for determining the user's 5-star reliability rating.
    long countByUserIdAndCompleted(Long userId, Boolean completed);
}