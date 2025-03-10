package com.devteria.Demo_Spring_boot.configuration;

import com.devteria.Demo_Spring_boot.constaint.PredefinedRole;
import com.devteria.Demo_Spring_boot.entity.Role;
import com.devteria.Demo_Spring_boot.entity.User;
import com.devteria.Demo_Spring_boot.repository.RoleRepository;
import com.devteria.Demo_Spring_boot.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

//    @Autowired // có các annotation ở trên nên không cần autowired
    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @NonFinal
    static final String ADMIN_EMAIL = "adminexample@gmail.com";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository){
        return args -> {
            log.info("Initializing application..... Please wait.");

//            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()){
//
////                var roles = new HashSet<String>();
////                roles.add(Role.ADMIN.name());
////                User user = User.builder()
////                        .username("admin")
////                        .password(passwordEncoder.encode("admin"))
//////                        .roles(roles)//truyền role vào
////                        .build();
////                userRepository.save(user);
////                log.warn("admin has been created with default password is admin, please change password.");
//                roleRepository.save(Role.builder()
//                        .name(PredefinedRole.USER_ROLE)
//                        .description("User role")
//                        .build());
//
//                Role adminRole = roleRepository.save(Role.builder()
//                        .name(PredefinedRole.ADMIN_ROLE)
//                        .description("Admin role")
//                        .build());

            if (!roleRepository.existsByName(PredefinedRole.USER_ROLE)) {
                roleRepository.save(Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());
            }

            if(!roleRepository.existsByName(PredefinedRole.ADMIN_ROLE)){
                Role adminRole = roleRepository.save(Role.builder()
                        .name(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .email(ADMIN_EMAIL)
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password is 'admin', please change it");
            }
            log.info("Application initialization completed ........");
        };
    }
}
