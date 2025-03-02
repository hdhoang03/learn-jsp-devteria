package com.devteria.Demo_Spring_boot.repository;

import com.devteria.Demo_Spring_boot.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
