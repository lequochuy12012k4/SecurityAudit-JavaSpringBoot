package com.javasecurityaudit.jsa_core.service.impl;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javasecurityaudit.jsa_core.dto.request.CreateUserRequest;
import com.javasecurityaudit.jsa_core.dto.response.UserResponse;
import com.javasecurityaudit.jsa_core.entity.Role;
import com.javasecurityaudit.jsa_core.entity.User;
import com.javasecurityaudit.jsa_core.enums.RoleType;
import com.javasecurityaudit.jsa_core.exception.AppException;
import com.javasecurityaudit.jsa_core.exception.ErrorCode;
import com.javasecurityaudit.jsa_core.mapper.UserMapper;
import com.javasecurityaudit.jsa_core.repository.RoleRepository;
import com.javasecurityaudit.jsa_core.repository.UserRepository;
import com.javasecurityaudit.jsa_core.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role defaultRole = roleRepository.findByName(RoleType.ROLE_USER.name())
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(RoleType.ROLE_USER.name())
                                .description("Mặc định cho người dùng hệ thống")
                                .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getMyInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }
}