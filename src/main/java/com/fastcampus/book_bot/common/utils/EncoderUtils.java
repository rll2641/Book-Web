package com.fastcampus.book_bot.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EncoderUtils {

    private final PasswordEncoder passwordEncoder;

    /* 패스워드 인코더 */
    public String passwordEncode(String password) {
        return passwordEncoder.encode(password);
    }

    /* 패스워드 검증 */
    public boolean validatePassword(String password, String encodePassword) {
        return passwordEncoder.matches(password, encodePassword);
    }
}
