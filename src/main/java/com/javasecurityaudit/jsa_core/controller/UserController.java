package com.javasecurityaudit.jsa_core.controller;

import com.javasecurityaudit.jsa_core.base.response.BaseResponse;
import com.javasecurityaudit.jsa_core.config.annotation.LogActivity;
import com.javasecurityaudit.jsa_core.dto.request.*;
import com.javasecurityaudit.jsa_core.dto.response.UserResponse;
import com.javasecurityaudit.jsa_core.enums.AuditAction;
import com.javasecurityaudit.jsa_core.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MessageSource messageSource;

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    @PostMapping("/create")
    @LogActivity(action = AuditAction.USER_CREATE, description = "Tạo tài khoản người dùng mới")
    public BaseResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return BaseResponse.success(getMessage("success.user.created"), userService.createUser(request));
    }

    @GetMapping("/my-info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @LogActivity(action = AuditAction.USER_GET_MY_INFO, description = "Người dùng tự xem thông tin cá nhân")
    public BaseResponse<UserResponse> getMyInfo() {
        return BaseResponse.success(userService.getMyInfo());
    }

    @PutMapping("/update-my-info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @LogActivity(action = AuditAction.USER_UPDATE, description = "Người dùng tự cập nhật thông tin cá nhân")
    public BaseResponse<UserResponse> updateMyInfo(@Valid @RequestBody UpdateMyInfoRequest request) {
        return BaseResponse.success(getMessage("success.user.updated"), userService.updateMyInfo(request));
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @LogActivity(action = AuditAction.USER_UPDATE, description = "Người dùng tự đổi mật khẩu")
    public BaseResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return BaseResponse.success(getMessage("success.password.changed"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @LogActivity(action = AuditAction.ADMIN_GET_ALL_USERS, description = "Admin xem danh sách người dùng")
    public BaseResponse<List<UserResponse>> getAllUsers() {
        return BaseResponse.success(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogActivity(action = AuditAction.ADMIN_GET_USER, description = "Admin xem thông tin người dùng")
    public BaseResponse<UserResponse> getUser(@PathVariable String userId) {
        return BaseResponse.success(userService.getUser(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogActivity(action = AuditAction.ADMIN_UPDATE_USER, description = "Admin cập nhật thông tin chi tiết người dùng")
    public BaseResponse<UserResponse> adminUpdateUser(
            @PathVariable String userId,
            @RequestBody AdminUpdateUserRequest request) {
        return BaseResponse.success(getMessage("success.user.updated"), userService.adminUpdateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogActivity(action = AuditAction.ADMIN_DELETE_USER, description = "Admin xóa người dùng")
    public BaseResponse<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return BaseResponse.success(getMessage("success.user.deleted"));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @LogActivity(action = AuditAction.ADMIN_UPDATE_STATUS_USER, description = "Admin thay đổi trạng thái tài khoản")
    public BaseResponse<UserResponse> updateUserStatus(
            @PathVariable String userId,
            @RequestBody UpdateUserStatusRequest request) {
        return BaseResponse.success(getMessage("success.status.updated"),
                userService.updateUserStatus(userId, request));
    }
}