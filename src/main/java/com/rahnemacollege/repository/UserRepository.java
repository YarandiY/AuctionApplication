package com.rahnemacollege.repository;

import com.rahnemacollege.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);


}
