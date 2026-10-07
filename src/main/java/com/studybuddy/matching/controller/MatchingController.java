package com.studybuddy.matching.controller;

import com.studybuddy.account.model.User;
import com.studybuddy.matching.service.MatchingService;
import com.studybuddy.matching.model.Meeting;
import com.studybuddy.matching.dto.JoinMeetingResponse;
import com.studybuddy.matching.dto.LeaveMeetingResponse;
import com.studybuddy.matching.dto.Participant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

// MatchingController manages the lifecycle of study sessions between buddies.
// It implements the core matching logic described in the Requirements Document Section 3.2.2.
// These endpoints facilitate the transition from searching for a buddy to starting a real-time Focus Session.
@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    // Final reference to the service layer handling the complex matching and meeting state logic.
    private final MatchingService matchingService;

    // Constructor injection ensures that the controller is always initialized with its required service dependency.
    // This follows the dependency injection best practices for maintainable and testable code[cite: 3, 4].
    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    // This endpoint handles the request to join a study session based on a specified duration.
    // It aligns with the "Meeting & Matching" requirement where users specify their availability (UI-2.1).
    // The duration parameter allows the system to pair users with similar study goals (Section 3.2.2).
    @PostMapping("/join/{duration}")
    public ResponseEntity<?> joinMeeting(
            @PathVariable int duration,
            @AuthenticationPrincipal User user
    ) {
        try {
            // Invokes the service logic to either find an existing session or create a new one.
            // This represents the "Waiting for Buddy" state described in the UI flow.
            Meeting meeting = matchingService.joinMeeting(user, duration);

            // Converts the set of User entities into a set of Participant DTOs.
            // This ensures only necessary information (ID and Username) is sent to the UI, as per Section 3.2.3.
            Set<Participant> participants = meeting.getUsers()
                    .stream()
                    .map(u -> new Participant(u.getId(), u.getUsername()))
                    .collect(Collectors.toSet());

            // Returns a successful response containing the meeting channel and participant list.
            // The channelName is critical for the client to connect to the real-time communication provider.
            return ResponseEntity.ok(
                    new JoinMeetingResponse(
                            String.valueOf(meeting.getMeetingId()),
                            meeting.getChannelName(),
                            meeting.getDuration(),
                            participants
                    )
            );
        } catch (Exception e) {
            // Handles any matching failures, such as server-side errors or invalid session states.
            return ResponseEntity.internalServerError()
                    .body("Failed to join meeting: " + e.getMessage());
        }
    }

    // This endpoint handles the voluntary exit of a user from an active study session.
    // It corresponds to the "Leaving Early" logic mentioned in Section 3.2.3 of the requirements.
    // Users who leave early may have their rating impacted as part of the platform's reliability metrics (FS-6).
    @PostMapping("/leave/{channelName}")
    public ResponseEntity<LeaveMeetingResponse> leaveMeeting(
            @PathVariable String channelName,
            @AuthenticationPrincipal User user
    ) {
        try {
            // Notifies the service layer that the user is disconnecting from the specified channel.
            // This allows the system to update the meeting status and notify the remaining buddy.
            matchingService.leaveMeeting(user, channelName);
        } catch (Exception e) {
            // Failures during the leave process are handled silently to ensure a smooth client-side transition.
            // The user may have already been disconnected due to network issues.
        }
        // Confirms to the client that the leave request has been processed.
        return ResponseEntity.ok(
                new LeaveMeetingResponse(true, "Left meeting successfully.")
        );
    }
}
