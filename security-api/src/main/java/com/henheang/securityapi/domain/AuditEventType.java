package com.henheang.securityapi.domain;

public enum AuditEventType {
    SIGNUP,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    ACCOUNT_LOCKED,
    LOGOUT,
    TOKEN_REVOKED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_COMPLETED,
    MFA_ENABLED,
    MFA_DISABLED,
    MFA_CHALLENGE_FAILED
}
