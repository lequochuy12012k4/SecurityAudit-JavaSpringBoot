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
    UNCATEGORIZED_EXCEPTION(9999, "error.uncategorized", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001, "error.user.existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "error.user.not.existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1003, "error.unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "error.unauthorized", HttpStatus.FORBIDDEN),
    INVALID_KEY(1005, "error.invalid.key", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1006, "error.invalid.credentials", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1007, "error.email.existed", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1008, "error.invalid.password", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(1009, "error.role.not.existed", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_YOURSELF(1010, "error.cannot.delete.yourself", HttpStatus.BAD_REQUEST),
    ACCOUNT_DISABLED(1011, "error.account.disabled", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(1012, "error.account.locked", HttpStatus.FORBIDDEN),
    TOO_MANY_LOGIN_ATTEMPTS(1013, "error.too.many.login.attempts", HttpStatus.FORBIDDEN),
    ;

    int code;
    String messageKey;
    HttpStatus statusCode;
}