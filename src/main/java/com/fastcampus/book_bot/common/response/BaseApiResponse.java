package com.fastcampus.book_bot.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseApiResponse {

    protected final boolean success;
    protected final String message;
    protected final LocalDateTime timestamp;
}
