package com.javasecurityaudit.jsa_core.base.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    @Builder.Default
    int code = 1000;
    
    String message;
    T result;
    public static <T> BaseResponse<T> success(T result) {
        return BaseResponse.<T>builder()
                .code(1000)
                .message("Success")
                .result(result)
                .build();
    }
    public static <T> BaseResponse<T> success(String message, T result) {
        return BaseResponse.<T>builder()
                .code(1000)
                .message(message)
                .result(result)
                .build();
    }
    public static <T> BaseResponse<T> success(String message) {
        return BaseResponse.<T>builder()
                .code(1000)
                .message(message)
                .build();
    }
    public static <T> BaseResponse<T> error(int code, String message) {
        return BaseResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
