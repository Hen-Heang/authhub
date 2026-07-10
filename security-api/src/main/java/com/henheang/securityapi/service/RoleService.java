package com.henheang.securityapi.service;


import com.henheang.securityapi.domain.Role;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {

    Role getOrCreateRole(String roleName);
}
