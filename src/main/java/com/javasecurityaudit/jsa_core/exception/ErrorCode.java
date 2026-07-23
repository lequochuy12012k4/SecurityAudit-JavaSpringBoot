package com.javasecurityaudit.jsa_core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001, "Người dùng đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1003, "Xác thực không thành công hoặc Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Bạn không có quyền truy cập tài nguyên này", HttpStatus.FORBIDDEN),
    INVALID_KEY(1005, "Mã lỗi không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1006, "Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1007, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final String message;
    private final HttpStatus statusCode;
}