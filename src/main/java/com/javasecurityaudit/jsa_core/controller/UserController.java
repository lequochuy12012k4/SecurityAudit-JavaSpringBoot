package com.javasecurityaudit.jsa_core.controller;

import com.javasecurityaudit.jsa_core.base.response.BaseResponse;
import com.javasecurityaudit.jsa_core.config.annotation.LogActivity;
import com.javasecurityaudit.jsa_core.dto.request.CreateUserRequest;
import com.javasecurityaudit.jsa_core.dto.response.UserResponse;
import com.javasecurityaudit.jsa_core.enums.AuditAction;
import com.javasecurityaudit.jsa_core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    @LogActivity(action = AuditAction.USER_CREATE, description = "Tạo mới người dùng")
    public BaseResponse<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        return BaseResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping("/my-info")
    public BaseResponse<UserResponse> getMyInfo() {
       return BaseResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }
    
}