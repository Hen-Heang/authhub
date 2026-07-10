package com.henheang.commonapi.components.common.api;

import lombok.Getter;

@Getter
public enum ExitCode {
    SUCCESS(200, "Success"),

    // Authentication Errors (1000-1099)
    AUTHENTICATION_FAILED(1000, "Authentication failed"),
    INVALID_CREDENTIALS(1001, "Invalid username or password"),
    ACCOUNT_LOCKED(1002, "Account has been locked"),
    ACCOUNT_DISABLED(1003, "Account is disabled"),
    TOKEN_EXPIRED(1004, "Authentication token has expired"),
    TOKEN_INVALID(1005, "Invalid authentication token"),
    INSUFFICIENT_PERMISSIONS(1006, "Insufficient permissions for this operation"),
    MFA_REQUIRED(1007, "Multi-factor authentication code required"),
    MFA_CODE_INVALID(1008, "Invalid multi-factor authentication code"),

    // Registration Errors (1100-1199)
    REGISTRATION_FAILED(1100, "Registration failed"),
    EMAIL_ALREADY_EXISTS(1101, "Email already exists"),
    USERNAME_ALREADY_EXISTS(1102, "Username already exists"),
    PASSWORD_TOO_WEAK(1103, "Password does not meet strength requirements"),

    // Password Reset Errors (1200-1299)
    PASSWORD_RESET_FAILED(1200, "Password reset failed"),
    PASSWORD_RESET_TOKEN_EXPIRED(1201, "Password reset token has expired"),
    PASSWORD_RESET_TOKEN_INVALID(1202, "Invalid password reset token"),

    // OAuth Errors (1300-1399)
    OAUTH_ERROR(1300, "OAuth authentication error"),
    OAUTH_PROVIDER_NOT_SUPPORTED(1301, "OAuth provider not supported"),
    OAUTH_EMAIL_NOT_VERIFIED(1302, "Email not verified by OAuth provider"),

    // System Errors (5000-5999)
    SYSTEM_ERROR(5000, "System error"),
    DATABASE_ERROR(5001, "Database error"),
    SECURITY_ERROR(5002, "Security configuration error"),
    JWT_CONFIGURATION_ERROR(5003, "JWT configuration error"),

    // General Errors (9000-9999)
    UNKNOWN_ERROR(9999, "Unknown error");

    private final int code;
    private final String message;

    ExitCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ExitCode fromCode(int code) {
        for (ExitCode exitCode : ExitCode.values()) {
            if (exitCode.getCode() == code) {
                return exitCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}