package com.javasecurityaudit.jsa_core.service;

import java.util.List;

import com.javasecurityaudit.jsa_core.dto.request.AdminUpdateUserRequest;
import com.javasecurityaudit.jsa_core.dto.request.ChangePasswordRequest;
import com.javasecurityaudit.jsa_core.dto.request.CreateUserRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateMyInfoRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateUserStatusRequest;
import com.javasecurityaudit.jsa_core.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getMyInfo();

    UserResponse updateMyInfo(UpdateMyInfoRequest request);
    void changePassword(ChangePasswordRequest request);

    List<UserResponse> getAllUsers();
    UserResponse getUser(String userId);
    
    UserResponse adminUpdateUser(String userId, AdminUpdateUserRequest request);

    void deleteUser(String userId);
    UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request);
}