package com.fastcampus.book_bot.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SignupRequestDTO {

    /* 회원가입 필드 */
    private String name;
    private String nickname;
    private String email;
    private String password;
    private String passwordConfirm;
    private String phone;
    private LocalDate birthDate;
    private boolean agreeTerms;
    private boolean emailVerified;

    /* 이메일 검증 필드 */
    private String verificationCode;

    /* 로그인 검증 필드 */
    private boolean rememberMe;
}
