package com.megapro.invoicesync.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megapro.invoicesync.model.Role;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface RoleDb extends JpaRepository<Role, Long>{
    Optional<Role> findByRole(String name);
}
