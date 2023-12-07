package com.webservices.cloudwebapp.webapp.repository;

import com.webservices.cloudwebapp.webapp.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {

    // Logic to get a user in database by emailID/username
    @Query("select count(p) = 1 from User p where p.username = ?1")
    public boolean findByEmail(String username);

     User findUserByUsername(String username);




}
