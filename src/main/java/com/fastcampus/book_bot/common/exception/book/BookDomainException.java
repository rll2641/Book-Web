package com.fastcampus.book_bot.common.exception.book;

import com.fastcampus.book_bot.common.exception.BaseDomainException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class BookDomainException extends BaseDomainException {

    public BookDomainException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, "BOOK", httpStatus);
    }

    public BookDomainException(String message, String errorCode,
                               HttpStatus httpStatus, Map<String, Object> errorDetails) {
        super(message, errorCode, "BOOK", httpStatus, errorDetails);
    }

    /**
     * BAD_REQUEST (400) 예외 생성
     */
    public static BookDomainException badRequest(String message, String errorCode) {
        return new BookDomainException(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public static BookDomainException badRequest(String message, String errorCode, Map<String, Object> errorDetails) {
        return new BookDomainException(message, errorCode, HttpStatus.BAD_REQUEST, errorDetails);
    }

    /**
     * UNAUTHORIZED (401) 예외 생성
     */
    public static BookDomainException unauthorized(String message, String errorCode) {
        return new BookDomainException(message, errorCode, HttpStatus.UNAUTHORIZED);
    }

    public static BookDomainException unauthorized(String message, String errorCode, Map<String, Object> errorDetails) {
        return new BookDomainException(message, errorCode, HttpStatus.UNAUTHORIZED, errorDetails);
    }

    /**
     * FORBIDDEN (403) 예외 생성
     */
    public static BookDomainException forbidden(String message, String errorCode) {
        return new BookDomainException(message, errorCode, HttpStatus.FORBIDDEN);
    }

    public static BookDomainException forbidden(String message, String errorCode, Map<String, Object> errorDetails) {
        return new BookDomainException(message, errorCode, HttpStatus.FORBIDDEN, errorDetails);
    }

    /**
     * NOT_FOUND (404) 예외 생성
     */
    public static BookDomainException notFound(String message, String errorCode) {
        return new BookDomainException(message, errorCode, HttpStatus.NOT_FOUND);
    }

    public static BookDomainException notFound(String message, String errorCode, Map<String, Object> errorDetails) {
        return new BookDomainException(message, errorCode, HttpStatus.NOT_FOUND, errorDetails);
    }

    /**
     * CONFLICT (409) 예외 생성
     */
    public static BookDomainException conflict(String message, String errorCode) {
        return new BookDomainException(message, errorCode, HttpStatus.CONFLICT);
    }

    public static BookDomainException conflict(String message, String errorCode, Map<String, Object> errorDetails) {
        return new BookDomainException(message, errorCode, HttpStatus.CONFLICT, errorDetails);
    }

    /**
     * UNPROCESSABLE_ENTITY (422) 예외 생성
     */
    public static BookDomainException unprocessableEntity(String message, String errorCode) {
        return new BookDomainException(message, errorCode, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static BookDomainException unprocessableEntity(String message, String errorCode, Map<String, Object> errorDetails) {
        return new BookDomainException(message, errorCode, HttpStatus.UNPROCESSABLE_ENTITY, errorDetails);
    }

    /**
     * INTERNAL_SERVER_ERROR (500) 예외 생성
     */
    public static BookDomainException internalServerError(String message, String errorCode) {
        return new BookDomainException(message, errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static BookDomainException internalServerError(String message, String errorCode, Map<String, Object> errorDetails) {
        return new BookDomainException(message, errorCode, HttpStatus.INTERNAL_SERVER_ERROR, errorDetails);
    }

    /**
     * SERVICE_UNAVAILABLE (503) 예외 생성
     */
    public static BookDomainException serviceUnavailable(String message, String errorCode) {
        return new BookDomainException(message, errorCode, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public static BookDomainException serviceUnavailable(String message, String errorCode, Map<String, Object> errorDetails) {
        return new BookDomainException(message, errorCode, HttpStatus.SERVICE_UNAVAILABLE, errorDetails);
    }
}