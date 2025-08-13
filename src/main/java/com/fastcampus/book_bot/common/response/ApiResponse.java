package com.fastcampus.book_bot.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {

    /* ApiResponse 클래스는 API 응답의 기본 구조를 정의한다.
    * 성공 여부, 메시지, 데이터, 에러 코드 생성
    * 성공 응답과 에러 응답을 생성하는 정적 메서드를 제공한다.
    * */

    private Boolean success;
    private String message;
    private T data;
    private String errorCode;

    /* 성공 응답 (메시지) */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /* 성공 응답 (응답 데이터) */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /* 성공 응답 (응답 데이터, 메시지) */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /* 에러 응답 (메시지) */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    /* 에러 응답 (메세지, 에러 코드) */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
