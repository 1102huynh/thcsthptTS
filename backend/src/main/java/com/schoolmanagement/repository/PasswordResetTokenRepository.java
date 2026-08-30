package com.schoolmanagement.repository;

import com.schoolmanagement.entity.PasswordResetToken;
import com.schoolmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    // To invalidate every outstanding token for a user the moment they
    // request a new one, or successfully reset their password - an old,
    // still-unexpired link must stop working once a newer one exists.
    List<PasswordResetToken> findByUserAndUsedAtIsNull(User user);
}
