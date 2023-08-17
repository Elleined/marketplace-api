package com.elleined.marketplaceapi.repository;

import com.elleined.marketplaceapi.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.uuid = :uuid")
    Optional<User> fetchByUUID(@Param("uuid") String uuid);
}