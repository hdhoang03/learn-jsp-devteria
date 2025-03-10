package com.devteria.Demo_Spring_boot.repository;

import com.devteria.Demo_Spring_boot.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
//    Optional<Role> findByName(String name);//tìm role theo tên
}
