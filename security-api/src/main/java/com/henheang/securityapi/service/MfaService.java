package com.henheang.securityapi.service;

public interface MfaService {

    String generateSecret();

    String getOtpAuthUri(String accountEmail, String secret);

    boolean verifyCode(String secret, String code);
}
