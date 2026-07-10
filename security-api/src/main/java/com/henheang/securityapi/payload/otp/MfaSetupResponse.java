package com.henheang.securityapi.payload.otp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MfaSetupResponse {
    private String secret;
    private String otpAuthUri;
}
