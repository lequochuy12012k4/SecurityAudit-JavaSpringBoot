package com.javasecurityaudit.jsa_core.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.javasecurityaudit.jsa_core.base.response.BaseResponse;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt tất cả ngoại lệ chưa được phân loại (Uncaught Exceptions)
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<BaseResponse<Object>> handlingRuntimeException(Exception exception) {
        log.error("Unhandled Exception: ", exception);
        
        BaseResponse<Object> apiResponse = BaseResponse.builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .build();

        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode()).body(apiResponse);
    }

    // 2. Bắt Custom Business Exception (AppException)
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<BaseResponse<Object>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        BaseResponse<Object> apiResponse = BaseResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // 3. Bắt lỗi Phân quyền (Access Denied - 403 Forbidden)
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Object>> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        BaseResponse<Object> apiResponse = BaseResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // 4. Bắt lỗi Validation dữ liệu đầu vào (@Valid trong Request Body)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            // Nếu message truyền vào annotation validation là text bình thường thay vì enum key
        }

        BaseResponse<Object> apiResponse = BaseResponse.builder()
                .code(errorCode.getCode())
                .message(Objects.nonNull(errorCode) ? errorCode.getMessage() : enumKey)
                .build();

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = DisabledException.class)
    public ResponseEntity<BaseResponse<Object>> handlingDisabledException(DisabledException exception) {
        ErrorCode errorCode = ErrorCode.ACCOUNT_DISABLED;

        BaseResponse<Object> apiResponse = BaseResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = LockedException.class)
    public ResponseEntity<BaseResponse<Object>> handlingLockedException(LockedException exception) {
        ErrorCode errorCode = ErrorCode.ACCOUNT_LOCKED;
        BaseResponse<Object> apiResponse = BaseResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }
    

}