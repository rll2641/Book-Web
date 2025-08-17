package com.fastcampus.book_bot.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 응답의 기본 구조를 제공하는 추상 클래스
 * 성공 여부, 메시지, 응답 시각 포함
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseApiResponse {

    protected final boolean success;
    protected final String message;
    protected final LocalDateTime timestamp;
}
