package com.elleined.marketplaceapi.repository;

import com.elleined.marketplaceapi.model.Moderator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;
import java.util.List;

public interface ModeratorRepository extends JpaRepository<Moderator, Integer> {

    @Query("SELECT m.moderatorCredential.email FROM Moderator m")
    List<String> fetchAllEmail();

    @Query("SELECT m FROM Moderator m WHERE m.moderatorCredential.email = :email")
    Moderator fetchByEmail(@Param("email") String email);
}