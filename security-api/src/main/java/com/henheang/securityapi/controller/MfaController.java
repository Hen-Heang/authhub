package com.henheang.securityapi.controller;

import com.henheang.commonapi.components.common.api.ExitCode;
import com.henheang.securityapi.domain.AuditEventType;
import com.henheang.securityapi.domain.User;
import com.henheang.securityapi.exception.AuthException;
import com.henheang.securityapi.payload.otp.MfaCodeRequest;
import com.henheang.securityapi.payload.otp.MfaSetupResponse;
import com.henheang.securityapi.payload.otp.MfaVerifyRequest;
import com.henheang.securityapi.security.UserPrincipal;
import com.henheang.securityapi.service.AuditLogService;
import com.henheang.securityapi.service.AuthService;
import com.henheang.securityapi.service.MfaService;
import com.henheang.securityapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/mfa")
@RequiredArgsConstructor
public class MfaController extends BaseController {

    private final MfaService mfaService;
    private final UserService userService;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    // Generates a new TOTP secret and returns the otpauth:// URI for the user
    // to scan into an authenticator app. MFA stays disabled until /enable
    // confirms the user actually holds a working authenticator.
    @PostMapping("/setup")
    public Object setup(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getUserById(userPrincipal.getId());
        String secret = mfaService.generateSecret();
        user.setMfaSecret(secret);
        user.setMfaEnabled(false);
        userService.saveUser(user);
        return ok(new MfaSetupResponse(secret, mfaService.getOtpAuthUri(user.getEmail(), secret)));
    }

    @PostMapping("/enable")
    public Object enable(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody MfaCodeRequest request) {
        User user = userService.getUserById(userPrincipal.getId());
        if (user.getMfaSecret() == null || !mfaService.verifyCode(user.getMfaSecret(), request.getCode())) {
            throw new AuthException(ExitCode.MFA_CODE_INVALID);
        }
        user.setMfaEnabled(true);
        userService.saveUser(user);
        auditLogService.log(AuditEventType.MFA_ENABLED, user.getId(), user.getEmail());
        return ok();
    }

    @PostMapping("/disable")
    public Object disable(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getUserById(userPrincipal.getId());
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userService.saveUser(user);
        auditLogService.log(AuditEventType.MFA_DISABLED, user.getId(), user.getEmail());
        return ok();
    }

    // Called after /api/auth/login returns mfaRequired=true, in place of a
    // normal login response - exchanges the short-lived challenge token plus
    // a valid TOTP code for real access/refresh tokens.
    @PostMapping("/verify")
    public Object verify(@Valid @RequestBody MfaVerifyRequest request) {
        return ok(authService.verifyMfaAndIssueTokens(request.getMfaToken(), request.getCode()));
    }
}
