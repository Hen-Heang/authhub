package com.henheang.securityapi.repository;


import com.henheang.securityapi.domain.PasswordResetToken;
import com.henheang.securityapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    List<PasswordResetToken> findByUser(User user);

//    Delete all expired tokens
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDateTime < ?1")
    void deleteAllExpiredTokens(LocalDateTime now);

    @Override
    Optional<PasswordResetToken> findById(Long aLong);


    //    Find by token
    Optional<PasswordResetToken> findByToken(String token);

}
