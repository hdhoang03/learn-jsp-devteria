package com.devteria.Demo_Spring_boot.repository;

import com.devteria.Demo_Spring_boot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    Optional<User> findByLastName(String lastName);
}
