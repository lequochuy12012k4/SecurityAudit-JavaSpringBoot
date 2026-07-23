package com.javasecurityaudit.jsa_core.service;

import com.javasecurityaudit.jsa_core.dto.request.CreateUserRequest;
import com.javasecurityaudit.jsa_core.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getMyInfo();
}