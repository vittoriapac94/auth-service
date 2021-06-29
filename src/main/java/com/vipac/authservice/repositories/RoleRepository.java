package com.vipac.authservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vipac.authservice.domains.Role;

public interface RoleRepository extends MongoRepository<Role, String> {
    
    Role findByRole(String role);
    
}