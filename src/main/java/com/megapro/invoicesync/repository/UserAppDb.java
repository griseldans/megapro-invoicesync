package com.megapro.invoicesync.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.megapro.invoicesync.model.UserApp;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface UserAppDb extends JpaRepository<UserApp, UUID>{
    UserApp findByEmail(String email);
    @Query("SELECT u FROM UserApp u WHERE u.role.role = :roleName")
    List<UserApp> findByRoleName(@Param("roleName") String roleName);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM UserApp u WHERE u.role.role IN :roleNames")
    List<UserApp> findByRoleNames(@Param("roleNames") List<String> roleNames);
}