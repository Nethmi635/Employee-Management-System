package com.example.ems.repository;

import com.example.ems.domain.user.PasswordResetToken;
import com.example.ems.domain.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findByUser(UserAccount user);
    List<PasswordResetToken> findByUsedFalse();
    List<PasswordResetToken> findByExpiryDateBefore(java.time.LocalDateTime date);
}
