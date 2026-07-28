package com.javasecurityaudit.jsa_core.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001, "Người dùng đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1003, "Xác thực không thành công hoặc Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Bạn không có quyền truy cập tài nguyên này", HttpStatus.FORBIDDEN),
    INVALID_KEY(1005, "Mã lỗi không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1006, "Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1007, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1008, "Mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(1009, "Vai trò không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_YOURSELF(1010, "Bạn không thể xóa chính mình", HttpStatus.BAD_REQUEST),
    ACCOUNT_DISABLED(1011,"Tài khoản chưa được kích hoạt hoặc đã bị vô hiệu hóa", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(1012,"Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),
    TOO_MANY_LOGIN_ATTEMPTS(1013, "Bạn đã vượt quá số lần đăng nhập cho phép. Vui lòng thử lại sau!", HttpStatus.FORBIDDEN),
    ;

    int code;
    String message;
    HttpStatus statusCode;
}