package com.devteria.Demo_Spring_boot.repository;

import com.devteria.Demo_Spring_boot.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

}
