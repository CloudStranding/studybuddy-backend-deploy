package com.studybuddy.matching.repository;

import com.studybuddy.account.model.User;
import com.studybuddy.matching.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// MeetingRepository provides the abstraction layer for database operations on Meeting entities.
// It supports the core "Meeting & Matching" functional requirements described in Section 3.2.2.
// This interface allows the system to efficiently query active study sessions based on user preferences.
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // Checks if a specific User is already an active participant in any existing meeting.
    // This is used to prevent a user from joining multiple sessions simultaneously, ensuring session integrity.
    boolean existsByUsersContaining(User user);

    // Retrieves a meeting session based on its unique real-time communication channel name.
    // This is essential for the "Leave Meeting" functionality to identify which session a user is exiting.
    Optional<Meeting> findByChannelName(String channelName);

    // This query is the heart of the matching algorithm described in the Requirements Document.
    // It finds available meetings that match the user's requested duration and still have space for a buddy.
    // According to Section 3.2.2, this enables the system to pair students with similar study goals.
    List<Meeting> findByDurationBetweenAndParticipantCountLessThan(
            int minDuration,
            int maxDuration,
            int maxParticipants
    );
}
