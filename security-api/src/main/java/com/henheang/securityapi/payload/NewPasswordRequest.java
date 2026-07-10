package com.henheang.securityapi.payload;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class NewPasswordRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = " New Password is required")
    @Size(min = 8, max = 50, message = "Email must be 50 characters or less")
    private String newPassword;

}
