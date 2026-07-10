package com.henheang.securityapi.config;

import com.henheang.securityapi.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer  implements CommandLineRunner {

    private final RoleService roleService;
    @Override
    public void run(String... args) {
        // Initialize roles
        roleService.getOrCreateRole("ROLE_USER");
        roleService.getOrCreateRole("ROLE_ADMIN");
    }

}
