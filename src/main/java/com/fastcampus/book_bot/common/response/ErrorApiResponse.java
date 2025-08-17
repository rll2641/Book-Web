package com.fastcampus.book_bot.common.response;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 에러에 대한 API 응답을 나타내는 클래스
 */
@Getter
public class ErrorApiResponse extends BaseApiResponse {

    private final String errorCode;
    private final Map<String, Object> errorDetail;

    public ErrorApiResponse(String message, String errorCode, Map<String, Object> errorDetail) {
        super(false, message, LocalDateTime.now());
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public static ErrorApiResponse message(String message) {
        return new ErrorApiResponse(message, null, null);
    }

    public static ErrorApiResponse of(String message, String errorCode) {
        return new ErrorApiResponse(message, errorCode, null);
    }

    /**
     * @param message 응답 메시지
     * @param errorCode 에러 코드
     * @param errorDetail 상세한 에러 정보 (key-value 형태)
     */
    public static ErrorApiResponse of(String message, String errorCode, Map<String, Object> errorDetail) {
        return new ErrorApiResponse(message, errorCode, errorDetail);
    }
}
