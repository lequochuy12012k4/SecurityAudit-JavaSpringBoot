package com.javasecurityaudit.jsa_core.exception;

import com.javasecurityaudit.jsa_core.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt lỗi Validation dữ liệu đầu vào (@Valid trong Request Body)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handlingValidation(
            MethodArgumentNotValidException exception, 
            HttpServletRequest request) {

        String messageOrKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();
        String finalMessage = messageOrKey;
        int errorCodeValue = 1001; // Mã lỗi mặc định cho Invalid Input (hoặc tùy chỉnh theo ErrorCode của bạn)

        try {
            ErrorCode errorCode = ErrorCode.valueOf(messageOrKey);
            finalMessage = errorCode.getMessage();
            errorCodeValue = errorCode.getCode();
        } catch (IllegalArgumentException e) {
            // Giữ nguyên message thuần nếu không phải Enum key
        }

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .code(errorCodeValue) // Gắn mã code
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(finalMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 2. Bắt tất cả ngoại lệ chưa được phân loại (Uncaught Exceptions - Status 500)
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiErrorResponse> handlingRuntimeException(
            Exception exception, 
            HttpServletRequest request) {

        log.error("Unhandled Exception: ", exception);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode()) // Gắn mã code từ ErrorCode
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // 3. Bắt Custom Business Exception (AppException)
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiErrorResponse> handlingAppException(
            AppException exception, 
            HttpServletRequest request) {

        ErrorCode errorCode = exception.getErrorCode();

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatusCode().value())
                .code(errorCode.getCode()) // Gắn mã code từ ErrorCode
                .error(errorCode.getStatusCode().getReasonPhrase())
                .message(errorCode.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(errorResponse);
    }

    // 4. Bắt lỗi Phân quyền (Access Denied - 403 Forbidden)
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handlingAccessDeniedException(
            AccessDeniedException exception, 
            HttpServletRequest request) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .code(ErrorCode.UNAUTHORIZED.getCode()) // Hoặc tạo ErrorCode riêng cho Forbidden
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ErrorCode.UNAUTHORIZED.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(value = DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handlingDisabledException(
            DisabledException exception, 
            HttpServletRequest request) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .code(ErrorCode.ACCOUNT_DISABLED.getCode())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ErrorCode.ACCOUNT_DISABLED.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(value = LockedException.class)
    public ResponseEntity<ApiErrorResponse> handlingLockedException(
            LockedException exception, 
            HttpServletRequest request) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .code(ErrorCode.ACCOUNT_LOCKED.getCode())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ErrorCode.ACCOUNT_LOCKED.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}