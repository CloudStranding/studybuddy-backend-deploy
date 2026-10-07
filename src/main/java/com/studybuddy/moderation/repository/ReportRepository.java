package com.studybuddy.moderation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.studybuddy.moderation.model.Report;

// ReportRepository provides the data access layer for misconduct reports within the Study Buddy platform.
// It enables the persistence and retrieval of report data as required for the Moderation system.
// By extending JpaRepository, it supports standard CRUD operations necessary for managing the moderation queue[cite: 5].
public interface ReportRepository extends JpaRepository<Report, Long> {

    // This interface automatically inherits methods like save(), findAll(), and findById() from JpaRepository.
    // These methods are utilized by the ModerationController to store new flags and display the Admin Dashboard.
    // Proper maintenance of this repository ensures that user reports are accurately recorded for administrator review.
}