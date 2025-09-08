package com.fastcampus.book_bot.common.advice;

import com.fastcampus.book_bot.common.exception.BaseDomainException;
import com.fastcampus.book_bot.common.response.ErrorApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 도메인 에러 핸들러
     * @param ex 도메인 에러
     * */
    @ExceptionHandler(BaseDomainException.class)
    public ResponseEntity<ErrorApiResponse> handleDomainException(BaseDomainException ex) {
        log.warn("DOMAIN EXCEPTION OCCURRED: [{}] {} - {}",
                ex.getDomain(), ex.getErrorCode(), ex.getMessage());

        ErrorApiResponse response;

        if (ex.getErrorDetail() != null) {
            response = ErrorApiResponse.of(
                    ex.getMessage(),
                    ex.getErrorCode(),
                    ex.getErrorDetail()
            );
        } else {
            response = ErrorApiResponse.of(
                    ex.getMessage(),
                    ex.getErrorCode()
            );
        }

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }
}
