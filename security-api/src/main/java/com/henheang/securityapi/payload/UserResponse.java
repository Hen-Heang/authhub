package com.henheang.securityapi.payload;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor

public class UserResponse {

    private Long Id;

    private String name;

    private String email;

    private String phoneNumber;

    private Boolean emailVerified;

    private String imageUrl;

    private String provider;

@Builder
    public UserResponse(Long id, String name, String email,String phoneNumber, Boolean emailVerified, String imageUrl, String provider) {
        Id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.emailVerified = emailVerified;
        this.imageUrl = imageUrl;
        this.provider = provider;
    }


}
