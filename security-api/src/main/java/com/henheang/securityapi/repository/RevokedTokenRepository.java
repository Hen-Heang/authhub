package com.henheang.securityapi.repository;

import com.henheang.securityapi.domain.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByJti(String jti);

    List<RevokedToken> findAllByExpiryDateBefore(Instant now);
}
