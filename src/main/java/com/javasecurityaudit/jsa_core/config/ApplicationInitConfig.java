package com.javasecurityaudit.jsa_core.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.javasecurityaudit.jsa_core.entity.Role;
import com.javasecurityaudit.jsa_core.entity.User;
import com.javasecurityaudit.jsa_core.enums.RoleType;
import com.javasecurityaudit.jsa_core.repository.RoleRepository;
import com.javasecurityaudit.jsa_core.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnProperty(prefix = "spring", value = "datasource.driver-class-name", havingValue = "org.postgresql.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) { // 👈 Inject thêm RoleRepository
        log.info("Initializing application data...");
        return args -> {

            Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN.name())
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name(RoleType.ROLE_ADMIN.name())
                                    .description("Quản trị viên hệ thống")
                                    .build()
                    ));
            if (userRepository.findByUsername("admin").isEmpty()) {
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);

                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("admin@gmail.com")
                        .fullName("Admin")
                        .roles(roles)
                        .enabled(true)
                        .accountNonLocked(true)
                        .build();

                userRepository.save(user);
                log.warn("Default admin user created (username: admin, password: admin). Please change it!");
            }
        };
    }
}