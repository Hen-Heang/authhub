package com.test.todoapi;

import com.henheang.securityapi.config.JwtConfig;
import com.henheang.securityapi.config.MfaEncryptionConfig;
import com.henheang.securityapi.exception.GlobalExceptionHandler;
import com.henheang.securityapi.security.CustomUserDetailsService;
import com.henheang.securityapi.security.JwtAuthenticationEntryPoint;
import com.henheang.securityapi.security.JwtAuthenticationFilter;
import com.henheang.securityapi.security.JwtTokenProvider;
import com.henheang.securityapi.security.crypto.MfaSecretConverter;
import com.henheang.securityapi.security.crypto.MfaSecretEncryptor;
import com.henheang.securityapi.service.impl.TokenBlacklistServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Only the JWT-validation slice of security-api is imported explicitly below
// (see SecurityConfig) - todoapi verifies tokens security-api issued, it
// does not run security-api's MFA/audit/rate-limiting/Google-OAuth services.
// MfaEncryptionConfig/MfaSecretConverter/MfaSecretEncryptor are pulled in
// anyway: User.mfaSecret is @Convert-encrypted at the JPA level (see
// User.java), so Hibernate needs that converter resolvable as a bean the
// moment the User entity is on the classpath, regardless of whether todoapi
// ever exercises MFA itself.
@SpringBootApplication
@EntityScan(basePackages = {"com.test.todoapi.domain", "com.henheang.securityapi.domain"})
@ComponentScan(basePackages = {"com.test.todoapi", "com.henheang.commonapi"})
@EnableJpaRepositories(
        basePackages = {"com.test.todoapi.repository", "com.henheang.securityapi.repository"})
@Import({
    JwtConfig.class,
    JwtTokenProvider.class,
    JwtAuthenticationFilter.class,
    JwtAuthenticationEntryPoint.class,
    CustomUserDetailsService.class,
    TokenBlacklistServiceImpl.class,
    GlobalExceptionHandler.class,
    MfaEncryptionConfig.class,
    MfaSecretConverter.class,
    MfaSecretEncryptor.class
})
public class TodoappApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoappApplication.class, args);
    }
}
