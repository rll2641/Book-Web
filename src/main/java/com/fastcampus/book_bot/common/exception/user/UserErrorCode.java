package com.fastcampus.book_bot.common.exception.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode {

    // ============== 인증/인가 관련 ==============
    LOGIN_FAILED("USER_LOGIN_FAILED", "로그인에 실패했습니다"),
    UNAUTHORIZED_ACCESS("USER_UNAUTHORIZED_ACCESS", "접근 권한이 없습니다"),
    INVALID_CREDENTIALS("USER_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다"),
    PASSWORD_MISMATCH("USER_PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다"),
    ACCESS_DENIED("USER_ACCESS_DENIED", "접근이 거부되었습니다"),

    // ============== 데이터 검증 관련 ==============
    INVALID_DATA("USER_INVALID_DATA", "유효하지 않은 데이터입니다"),
    INVALID_EMAIL_FORMAT("USER_INVALID_EMAIL_FORMAT", "올바르지 않은 이메일 형식입니다"),
    INVALID_PASSWORD_FORMAT("USER_INVALID_PASSWORD_FORMAT", "비밀번호 형식이 올바르지 않습니다"),
    INVALID_PHONE_FORMAT("USER_INVALID_PHONE_FORMAT", "올바르지 않은 전화번호 형식입니다"),
    INVALID_NICKNAME_FORMAT("USER_INVALID_NICKNAME_FORMAT", "올바르지 않은 닉네임 형식입니다"),

    // ============== 중복 관련 ==============
    EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다"),
    NICKNAME_ALREADY_EXISTS("USER_NICKNAME_ALREADY_EXISTS", "이미 존재하는 닉네임입니다"),

    // ============== 조회 관련 ==============
    NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
    EMAIL_NOT_FOUND("USER_EMAIL_NOT_FOUND", "해당 이메일의 사용자를 찾을 수 없습니다"),

    // ============== 이메일 인증 관련 ==============
    EMAIL_NOT_VERIFIED("USER_EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다"),
    VERIFICATION_CODE_EXPIRED("USER_VERIFICATION_CODE_EXPIRED", "인증코드가 만료되었습니다"),
    VERIFICATION_CODE_INVALID("USER_VERIFICATION_CODE_INVALID", "올바르지 않은 인증코드입니다"),
    EMAIL_SEND_FAILED("USER_EMAIL_SEND_FAILED", "이메일 전송에 실패했습니다"),

    // ============== 약관/정책 관련 ==============
    TERMS_NOT_AGREED("USER_TERMS_NOT_AGREED", "약관에 동의해야 합니다"),
    PRIVACY_POLICY_NOT_AGREED("USER_PRIVACY_POLICY_NOT_AGREED", "개인정보 처리방침에 동의해야 합니다"),

    // ============== 계정 상태 관련 ==============
    ACCOUNT_INACTIVE("USER_ACCOUNT_INACTIVE", "비활성화된 계정입니다"),
    ACCOUNT_SUSPENDED("USER_ACCOUNT_SUSPENDED", "정지된 계정입니다"),
    ACCOUNT_LOCKED("USER_ACCOUNT_LOCKED", "잠긴 계정입니다"),

    // ============== 시스템 에러 ==============
    SYSTEM_ERROR("USER_SYSTEM_ERROR", "시스템 오류가 발생했습니다"),
    DATABASE_ERROR("USER_DATABASE_ERROR", "데이터베이스 오류가 발생했습니다");

    private final String code;
    private final String message;
}
