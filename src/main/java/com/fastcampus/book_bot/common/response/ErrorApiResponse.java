package com.fastcampus.book_bot.common.response;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 에러 API 응답을 나타내는 클래스
 *
 * @param <T> 상세한 에러 정보 타입
 */
@Getter
public class ErrorApiResponse<T> extends BaseApiResponse {

    private final String errorCode;
    private final T errorDetail;

    public ErrorApiResponse(String message, String errorCode, T errorDetail) {
        super(false, message, LocalDateTime.now());
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public static <T> ErrorApiResponse<T> message(String message) {
        return new ErrorApiResponse<>(message, null, null);
    }

    public static <T> ErrorApiResponse<T> of(String message, String errorCode) {
        return new ErrorApiResponse<>(message, errorCode, null);
    }

    /**
     * @param message 응답 메시지
     * @param errorCode 에러 코드
     * @param errorDetail 상세한 에러 정보
     */
    public static <T> ErrorApiResponse<T> of(String message, String errorCode, T errorDetail) {
        return new ErrorApiResponse<>(message, errorCode, errorDetail);
    }
}
