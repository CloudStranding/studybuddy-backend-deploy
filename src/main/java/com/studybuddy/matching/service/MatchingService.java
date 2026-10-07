package com.studybuddy.matching.service;

import com.studybuddy.account.model.User;
import com.studybuddy.matching.model.Meeting;
import com.studybuddy.matching.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

// MatchingService handles the core algorithmic pairing and session management logic.
// It implements the functional requirements for matching students based on duration.
// This service ensures thread-safe transitions during the "Waiting for Buddy" phase.
@Service
public class MatchingService {

    // Defines the maximum number of students allowed in a single study session.
    // This constraint ensures an optimal focus environment as intended in the design.
    private static final int MAX_PARTICIPANTS = 10;

    // Persistence layer for managing meeting state and participant records.
    private final MeetingRepository meetingRepository;

    // Constructor-based dependency injection for better testability and maintenance[cite: 4, 7].
    public MatchingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    // Handles the logic for a user entering the matching pool or an existing session.
    // It fulfills the UI-2.1 requirement where users join based on study duration.
    @Transactional
    public Meeting joinMeeting(User user, int duration) {
        // Ensures a user is not linked to multiple sessions simultaneously, maintaining session integrity.
        removeUserFromAnyExistingMeeting(user);

        // Algorithm: Finds candidate meetings within a ±5 minute window of the requested duration.
        // This facilitates pairing students with similar study goals as specified in Section 3.2.2.
        List<Meeting> candidates = meetingRepository
                .findByDurationBetweenAndParticipantCountLessThan(
                        duration - 5, duration + 5, MAX_PARTICIPANTS);

        Meeting target = null;
        for (Meeting candidate : candidates) {
            int actualSize = candidate.getUsers().size();

            // Cleanup: Removes empty meeting records from the database to prevent "ghost" sessions.
            if (actualSize == 0) {
                meetingRepository.delete(candidate);
                continue;
            }

            // Sync: Corrects the participant count if it deviates from the actual collection size.
            if (candidate.getParticipantCount() != actualSize) {
                candidate.setParticipantCount(actualSize);
                meetingRepository.save(candidate);
            }

            // Skip sessions that have reached the maximum allowed capacity.
            if (actualSize >= MAX_PARTICIPANTS) {
                continue;
            }

            target = candidate;
            break;
        }

        // Force database synchronization before proceeding to create or update.
        meetingRepository.flush();

        // If no suitable meeting exists, initialize a new synchronized study session.
        if (target == null) {
            target = new Meeting();
            target.setChannelName(generateChannelName(duration));
            target.setDuration(duration);
        }

        // Update the meeting state with the new participant.
        target.addParticipant(user);
        target.setParticipantCount(target.getUsers().size());

        // Persist and return the updated meeting metadata for the Focus Session UI.
        return meetingRepository.save(target);
    }

    // Facilitates the voluntary exit of a user from a specific study channel.
    // This supports the "Leaving Early" logic which may impact user ratings.
    @Transactional
    public void leaveMeeting(User user, String channelName) {
        Meeting meeting = meetingRepository.findByChannelName(channelName)
                .orElse(null);
        if (meeting == null) return;

        // Securely remove the user from the session's participant list.
        removeUserById(meeting, user.getId());
        meeting.setParticipantCount(meeting.getUsers().size());

        // Lifecycle: If the session becomes empty, delete it; otherwise, update remaining buddy status.
        if (meeting.getUsers().isEmpty()) {
            meetingRepository.delete(meeting);
        } else {
            meetingRepository.save(meeting);
        }
    }

    // Internal safety mechanism to clean up stale user references in the meeting table.
    private void removeUserFromAnyExistingMeeting(User user) {
        List<Meeting> allMeetings = meetingRepository.findAll();
        for (Meeting m : allMeetings) {
            // Check if the user is present in the current meeting iteration.
            if (containsUserId(m, user.getId())) {
                removeUserById(m, user.getId());
                m.setParticipantCount(m.getUsers().size());

                // Perform database cleanup if the meeting has no participants left.
                if (m.getUsers().isEmpty()) {
                    meetingRepository.delete(m);
                } else {
                    meetingRepository.save(m);
                }
                meetingRepository.flush();
                return;
            }
        }
    }

    // Helper: Validates if a specific user ID exists within the meeting's participant set.
    private boolean containsUserId(Meeting meeting, Long userId) {
        for (User u : meeting.getUsers()) {
            if (u.getId().equals(userId)) return true;
        }
        return false;
    }

    // Helper: Safely removes a user by their ID using an iterator to avoid ConcurrentModificationException.
    private void removeUserById(Meeting meeting, Long userId) {
        Iterator<User> it = meeting.getUsers().iterator();
        while (it.hasNext()) {
            if (it.next().getId().equals(userId)) {
                it.remove();
                return;
            }
        }
    }

    // Generates a unique channel identifier for real-time communication.
    // Includes duration and a random UUID fragment to ensure uniqueness across the platform.
    private String generateChannelName(long duration) {
        String shortId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "study_" + duration + "_" + shortId;
    }
}
