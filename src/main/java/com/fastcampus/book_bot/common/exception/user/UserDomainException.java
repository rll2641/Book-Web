package com.fastcampus.book_bot.common.exception.user;

import com.fastcampus.book_bot.common.exception.BaseDomainException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class UserDomainException extends BaseDomainException {

    public UserDomainException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, "USER", httpStatus);
    }

    public UserDomainException(String message, String errorCode,
                               HttpStatus httpStatus, Map<String, Object> errorDetails) {
        super(message, errorCode, "USER", httpStatus, errorDetails);
    }

    // ============== HttpStatus 기반 범용 팩토리 메서드 ==============

    /**
     * BAD_REQUEST (400) 예외 생성
     */
    public static UserDomainException badRequest(String message, String errorCode) {
        return new UserDomainException(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public static UserDomainException badRequest(String message, String errorCode, Map<String, Object> errorDetails) {
        return new UserDomainException(message, errorCode, HttpStatus.BAD_REQUEST, errorDetails);
    }

    /**
     * UNAUTHORIZED (401) 예외 생성
     */
    public static UserDomainException unauthorized(String message, String errorCode) {
        return new UserDomainException(message, errorCode, HttpStatus.UNAUTHORIZED);
    }

    public static UserDomainException unauthorized(String message, String errorCode, Map<String, Object> errorDetails) {
        return new UserDomainException(message, errorCode, HttpStatus.UNAUTHORIZED, errorDetails);
    }

    /**
     * FORBIDDEN (403) 예외 생성
     */
    public static UserDomainException forbidden(String message, String errorCode) {
        return new UserDomainException(message, errorCode, HttpStatus.FORBIDDEN);
    }

    public static UserDomainException forbidden(String message, String errorCode, Map<String, Object> errorDetails) {
        return new UserDomainException(message, errorCode, HttpStatus.FORBIDDEN, errorDetails);
    }

    /**
     * NOT_FOUND (404) 예외 생성
     */
    public static UserDomainException notFound(String message, String errorCode) {
        return new UserDomainException(message, errorCode, HttpStatus.NOT_FOUND);
    }

    public static UserDomainException notFound(String message, String errorCode, Map<String, Object> errorDetails) {
        return new UserDomainException(message, errorCode, HttpStatus.NOT_FOUND, errorDetails);
    }

    /**
     * CONFLICT (409) 예외 생성
     */
    public static UserDomainException conflict(String message, String errorCode) {
        return new UserDomainException(message, errorCode, HttpStatus.CONFLICT);
    }

    public static UserDomainException conflict(String message, String errorCode, Map<String, Object> errorDetails) {
        return new UserDomainException(message, errorCode, HttpStatus.CONFLICT, errorDetails);
    }

    /**
     * UNPROCESSABLE_ENTITY (422) 예외 생성
     */
    public static UserDomainException unprocessableEntity(String message, String errorCode) {
        return new UserDomainException(message, errorCode, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static UserDomainException unprocessableEntity(String message, String errorCode, Map<String, Object> errorDetails) {
        return new UserDomainException(message, errorCode, HttpStatus.UNPROCESSABLE_ENTITY, errorDetails);
    }

    /**
     * INTERNAL_SERVER_ERROR (500) 예외 생성
     */
    public static UserDomainException internalServerError(String message, String errorCode) {
        return new UserDomainException(message, errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static UserDomainException internalServerError(String message, String errorCode, Map<String, Object> errorDetails) {
        return new UserDomainException(message, errorCode, HttpStatus.INTERNAL_SERVER_ERROR, errorDetails);
    }
}