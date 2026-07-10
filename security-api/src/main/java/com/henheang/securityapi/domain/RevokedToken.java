package com.henheang.securityapi.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "revoked_token")
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JWT "jti" claim - identifies the specific access token that was revoked.
    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    // The access token's own expiration. Once this passes, the blacklist entry
    // is dead weight - the token would be rejected as expired anyway - so the
    // cleanup job purges rows past this point instead of keeping them forever.
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;
}
