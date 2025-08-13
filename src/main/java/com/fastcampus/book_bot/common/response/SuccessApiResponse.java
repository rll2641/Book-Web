package com.fastcampus.book_bot.common.response;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 성공적인 API 응답을 나타내는 클래스
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
public class SuccessApiResponse<T> extends BaseApiResponse {

    private final T data;

    public SuccessApiResponse(String message, T data) {
        super(true, message, LocalDateTime.now());
        this.data = data;
    }

    public static <T> SuccessApiResponse<T> of(T data) {
        return new SuccessApiResponse<>(null, data);
    }

    /**
     * @param data 응답 데이터
     * @param message 응답 메시지
     */
    public static <T> SuccessApiResponse<T> of(T data, String message) {
        return new SuccessApiResponse<>(message, data);
    }

    public static <T> SuccessApiResponse<T> message(String message) {
        return new SuccessApiResponse<>(message, null);
    }
}