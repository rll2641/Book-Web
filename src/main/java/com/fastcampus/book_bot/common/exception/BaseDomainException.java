package com.fastcampus.book_bot.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseDomainException extends RuntimeException {
    protected final String errorCode;
    protected final String domain;
    protected final HttpStatus httpStatus;

    protected BaseDomainException(String message, String errorCode,
                                  String domain, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.domain = domain;
        this.httpStatus = httpStatus;
    }
}
