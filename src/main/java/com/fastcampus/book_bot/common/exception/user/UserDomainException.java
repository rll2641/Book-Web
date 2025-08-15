package com.fastcampus.book_bot.common.exception.user;

import com.fastcampus.book_bot.common.exception.BaseDomainException;
import org.springframework.http.HttpStatus;

public class UserDomainException extends BaseDomainException {

    public UserDomainException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, "USER", httpStatus);
    }

    public static UserDomainException notFound(Integer userId) {
        return new UserDomainException(
                "사용자를 찾을 수 없습니다. USER_ID: {}" + userId,
                "USER_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    public static UserDomainException emailDuplicate(String email) {
        return new UserDomainException(
                "이미 존재하는 이메일입니다: " + email,
                "EMAIL_ALREADY_EXISTS",
                HttpStatus.CONFLICT
        );
    }

    public static UserDomainException nicknameDuplicate(String nickname) {
        return new UserDomainException(
                "이미 존재하는 닉네임입니다: " + nickname,
                "NICKNAME_ALREADY_EXISTS",
                HttpStatus.CONFLICT
        );
    }

    public static UserDomainException unauthorized() {
        return new UserDomainException(
                "접근 권한이 없습니다",
                "UNAUTHORIZED_ACCESS",
                HttpStatus.FORBIDDEN
        );
    }

    public static UserDomainException invalidData(String field, String reason) {
        return new UserDomainException(
                String.format("유효하지 않은 데이터 - %s: %s", field, reason),
                "USER_INVALID_DATA",
                HttpStatus.BAD_REQUEST
        );
    }
}
