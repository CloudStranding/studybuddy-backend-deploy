package com.studybuddy.matching.service;

import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import com.studybuddy.matching.model.Meeting;
import com.studybuddy.matching.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MatchingService.
 * Tests join and leave meeting business logic directly.
 * Uses real database interactions with H2 in-memory database.
 */
@SpringBootTest
public class MatchingServiceTest {

    /** Service under test. */
    @Autowired
    private MatchingService matchingService;

    /** Repository for meeting database access during tests. */
    @Autowired
    private MeetingRepository meetingRepository;

    /** Repository for user database access during tests. */
    @Autowired
    private UserRepository userRepository;

    /** Password encoder for creating test users. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** First test user. */
    private User user1;

    /** Second test user. */
    private User user2;

    /**
     * Sets up test users before each test.
     * Clears meetings first to avoid foreign key violations.
     */
    @BeforeEach
    void setUp() {
        // Delete meetings first to avoid foreign key constraint violations
        meetingRepository.deleteAll();
        userRepository.deleteAll();

        // Create first test user
        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword(passwordEncoder.encode("Test@123"));
        user1 = userRepository.save(user1);

        // Create second test user
        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword(passwordEncoder.encode("Test@123"));
        user2 = userRepository.save(user2);
    }

    /**
     * Tests that a user can join a meeting successfully.
     * A new meeting should be created with the correct duration.
     */
    @Test
    void joinMeeting_CreatesNewMeeting() {
        // Act: user1 joins a 30 minute meeting
        Meeting meeting = matchingService.joinMeeting(user1, 30);

        // Assert: meeting created with correct properties
        assertNotNull(meeting);
        assertEquals(30, meeting.getDuration());
        assertTrue(meeting.getChannelName().startsWith("study_30_"));
        assertEquals(1, meeting.getUsers().size());
        assertTrue(meeting.getUsers().contains(user1));
    }

    /**
     * Tests that a second user joins an existing meeting with matching duration.
     * Both users should be in the same meeting.
     */
    @Test
    void joinMeeting_SecondUserJoinsExistingMeeting() {
        // Arrange: user1 already joined a meeting
        Meeting firstMeeting = matchingService.joinMeeting(user1, 30);

        // Act: user2 joins with same duration range
        Meeting secondMeeting = matchingService.joinMeeting(user2, 30);

        // Assert: both users in the same meeting
        assertEquals(firstMeeting.getMeetingId(), secondMeeting.getMeetingId());
        assertEquals(2, secondMeeting.getUsers().size());
    }

    /**
     * Tests that a user joining twice gets moved to the new meeting.
     * The user should be removed from the old meeting first.
     */
    @Test
    void joinMeeting_UserJoinsTwice_MovedToNewMeeting() {
        // Arrange: user1 joins first meeting
        Meeting firstMeeting = matchingService.joinMeeting(user1, 30);

        // Act: user1 joins again with different duration
        Meeting secondMeeting = matchingService.joinMeeting(user1, 60);

        // Assert: user1 is in new meeting, not old one
        assertNotEquals(firstMeeting.getMeetingId(), secondMeeting.getMeetingId());
        assertEquals(60, secondMeeting.getDuration());
        assertTrue(secondMeeting.getUsers().contains(user1));
    }

    /**
     * Tests that a user can leave a meeting successfully.
     * The meeting should be deleted when empty.
     */
    @Test
    void leaveMeeting_LastUser_DeletesMeeting() {
        // Arrange: user1 joins a meeting
        Meeting meeting = matchingService.joinMeeting(user1, 30);
        String channelName = meeting.getChannelName();

        // Act: user1 leaves the meeting
        matchingService.leaveMeeting(user1, channelName);

        // Assert: meeting is deleted since it's empty
        assertFalse(meetingRepository.findByChannelName(channelName).isPresent());
    }

    /**
     * Tests that meeting persists when one user leaves but others remain.
     * Only the leaving user should be removed.
     */
    @Test
    void leaveMeeting_NotLastUser_MeetingPersists() {
        // Arrange: user1 joins a meeting
        Meeting meeting = matchingService.joinMeeting(user1, 30);
        String channelName = meeting.getChannelName();

        // user2 joins the same meeting
        matchingService.joinMeeting(user2, 30);

        // Verify both users are in the meeting before leaving
        Meeting meetingBeforeLeave = meetingRepository
                .findByChannelName(channelName)
                .orElse(null);
        assertNotNull(meetingBeforeLeave);
        assertEquals(2, meetingBeforeLeave.getUsers().size());

        // Act: user1 leaves
        matchingService.leaveMeeting(user1, channelName);

        // Assert: meeting still exists with only user2
        Meeting remainingMeeting = meetingRepository
                .findByChannelName(channelName)
                .orElse(null);
        assertNotNull(remainingMeeting);
        assertEquals(1, remainingMeeting.getUsers().size());
    }

    /**
     * Tests that leaving a non-existent meeting does not throw an exception.
     * The service should handle this gracefully.
     */
    @Test
    void leaveMeeting_NonExistentChannel_NoException() {
        // Act & Assert: should not throw any exception
        assertDoesNotThrow(() ->
                matchingService.leaveMeeting(user1, "nonexistent_channel"));
    }

    /**
     * Tests that meetings with different durations are kept separate.
     * Users with different durations should not be matched together.
     */
    @Test
    void joinMeeting_DifferentDurations_SeparateMeetings() {
        // Act: users join with very different durations
        Meeting meeting1 = matchingService.joinMeeting(user1, 30);
        Meeting meeting2 = matchingService.joinMeeting(user2, 90);

        // Assert: users are in different meetings
        assertNotEquals(meeting1.getMeetingId(), meeting2.getMeetingId());
    }

    /**
     * Tests that participant count is correctly updated after joining.
     * The count should reflect the actual number of users.
     */
    @Test
    void joinMeeting_ParticipantCountUpdated() {
        // Act: two users join the same meeting
        matchingService.joinMeeting(user1, 30);
        Meeting meeting = matchingService.joinMeeting(user2, 30);

        // Assert: participant count is 2
        assertEquals(2, meeting.getParticipantCount());
    }
}
