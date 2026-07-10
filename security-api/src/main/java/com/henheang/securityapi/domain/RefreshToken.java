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
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SHA-256 hash of the raw token handed to the client. Only the hash is
    // persisted, so a database leak doesn't hand out usable refresh tokens.
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    // The raw, un-hashed token. Populated only in memory right after
    // generation so the caller can return it to the client - never persisted.
    @Transient
    private String rawToken;

    @Column(nullable = false)
    private Instant expiryDate;

    // Using ManyToOne since many refresh tokens can belong to one user
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;
}