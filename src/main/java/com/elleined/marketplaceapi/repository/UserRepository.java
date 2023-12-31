package com.elleined.marketplaceapi.repository;

import com.elleined.marketplaceapi.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE u.userDetails.firstName LIKE CONCAT('%', :username, '%') ORDER BY u.userDetails.firstName ASC")
    Set<User> searchByUserName(@Param("username") String username);

    @Query("select u from User u where u.userCredential.email = ?1")
    Optional<User> fetchByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.referralCode = :referralCode")
    Optional<User> fetchByReferralCode(@Param("referralCode") String referralCode);

    @Query("SELECT u.userCredential.email FROM User u")
    List<String> fetchAllEmail();

    @Query("SELECT u.userDetails.mobileNumber FROM User u")
    List<String> fetchAllMobileNumber();
}