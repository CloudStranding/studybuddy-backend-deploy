package com.studybuddy.account.repository;

import com.studybuddy.account.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used for login)
    Optional<User> findByEmail(String email);

    // Find user by username (used for login)
    Optional<User> findByUsername(String username);

    // Find user by either username or email (used for login)
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Check if email already exists (used for registration)
    boolean existsByEmail(String email);

    // Check if the username already exists (used for registration)
    boolean existsByUsername(String username);
}
