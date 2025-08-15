package com.fastcampus.book_bot.dto.user;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SignupRequestDTO {

    /* 회원가입 필드 */
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String passwordConfirm;

    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;

    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate;

    private boolean agreeTerms;

    private boolean emailVerified;

    /* 이메일 검증 필드 */
    private String verificationCode;

    /* 로그인 검증 필드 */
    private boolean rememberMe;
}
