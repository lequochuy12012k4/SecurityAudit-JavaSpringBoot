package com.javasecurityaudit.jsa_core.controller;

import com.javasecurityaudit.jsa_core.base.response.BaseResponse;
import com.javasecurityaudit.jsa_core.config.annotation.LogActivity;
import com.javasecurityaudit.jsa_core.dto.request.*;
import com.javasecurityaudit.jsa_core.dto.response.UserResponse;
import com.javasecurityaudit.jsa_core.enums.AuditAction;
import com.javasecurityaudit.jsa_core.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    
    @PostMapping("/create")
    @LogActivity(action = AuditAction.USER_CREATE, description = "Tạo tài khoản người dùng mới")
    public BaseResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return BaseResponse.success(userService.createUser(request));
    }

    // ================= DÀNH CHO CẢ USER VÀ ADMIN =================
    @GetMapping("/my-info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BaseResponse<UserResponse> getMyInfo() {
        return BaseResponse.success(userService.getMyInfo());
    }

    @PutMapping("/update-my-info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @LogActivity(action = AuditAction.USER_UPDATE, description = "Người dùng tự cập nhật thông tin cá nhân")
    public BaseResponse<UserResponse> updateMyInfo(@Valid @RequestBody UpdateMyInfoRequest request) {
        return BaseResponse.success(userService.updateMyInfo(request));
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @LogActivity(action = AuditAction.USER_UPDATE, description = "Người dùng tự đổi mật khẩu")
    public BaseResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return BaseResponse.success("Đổi mật khẩu thành công!");
    }

    // ================= CHỈ DÀNH CHO ADMIN =================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<List<UserResponse>> getAllUsers() {
        return BaseResponse.success(userService.getAllUsers());
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogActivity(action = AuditAction.USER_UPDATE, description = "Admin cập nhật thông tin chi tiết người dùng")
    public BaseResponse<UserResponse> adminUpdateUser(
            @PathVariable String userId,
            @RequestBody AdminUpdateUserRequest request) {
        return BaseResponse.success(userService.adminUpdateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogActivity(action = AuditAction.USER_DELETE, description = "Admin xóa người dùng")
    public BaseResponse<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return BaseResponse.success("Xóa người dùng thành công!");
    }
}