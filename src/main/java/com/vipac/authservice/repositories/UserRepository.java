package com.vipac.authservice.repositories;


import org.springframework.data.mongodb.repository.MongoRepository;

import com.vipac.authservice.domains.User;

public interface UserRepository extends MongoRepository<User, String> {
	    
	User findByEmail(String email);
	User findByMatricola(String matricola);
	    
}
