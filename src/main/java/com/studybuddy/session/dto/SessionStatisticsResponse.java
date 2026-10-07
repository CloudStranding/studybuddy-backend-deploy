package com.studybuddy.session.dto;

// SessionStatisticsResponse is a Data Transfer Object (DTO) used to provide a high-level
// summary of a user's study performance and engagement metrics.
// It directly supports the "User Dashboard" and "Statistics" features described in Section 3.2.1.
// This object aggregates historical session data into actionable insights for the student.
public class SessionStatisticsResponse {

    // FS-6: The calculated reliability score based on the ratio of completed to total sessions.
    // This rating (0.0 to 5.0) helps in buddy matching and platform trust as per Section 3.2.3.
    private double rating;

    // The cumulative amount of time the user has spent in successful "Focus Sessions" (in minutes).
    // This serves as a primary motivator for users to track their productivity growth over time.
    private long totalFocusTimeMinutes;

    // The total count of study sessions that were successfully finished without leaving early.
    // Higher counts directly contribute to a better platform standing and user reputation.
    private long completedCount;

    // The total number of sessions initiated by the user, including both finished and incomplete ones.
    // This value is the denominator used to determine the user's completion rate and rating.
    private long totalCount;

    // Constructor to initialize the statistics summary with calculated values from the FocusSessionService.
    // This ensures that the frontend receives a pre-processed snapshot of the user's achievements.
    public SessionStatisticsResponse(double rating, long totalFocusTimeMinutes,
                                     long completedCount, long totalCount) {
        this.rating = rating;
        this.totalFocusTimeMinutes = totalFocusTimeMinutes;
        this.completedCount = completedCount;
        this.totalCount = totalCount;
    }

    // Standard getter for the user's current platform rating.
    // Used by the UI to render the star-rating component on the profile screen.
    public double getRating() {
        return rating;
    }

    // Standard getter for the total time spent in focus.
    // Allows the frontend to display "Total Hours Studied" by converting these minutes.
    public long getTotalFocusTimeMinutes() {
        return totalFocusTimeMinutes;
    }

    // Standard getter for the number of completed sessions.
    // This provides evidence of the user's consistency within the Study Buddy community.
    public long getCompletedCount() {
        return completedCount;
    }

    // Standard getter for the total number of session attempts.
    // Essential for calculating and displaying progress bars or completion percentages in the UI.
    public long getTotalCount() {
        return totalCount;
    }
}