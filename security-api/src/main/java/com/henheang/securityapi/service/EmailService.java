package com.henheang.securityapi.service;

import org.springframework.stereotype.Service;

@Service

public interface EmailService {

    boolean sendPasswordResetEmail(String email, String name, String resetUrl);

}
