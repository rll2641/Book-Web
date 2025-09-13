package com.fastcampus.book_bot.service.auth;

import com.fastcampus.book_bot.common.exception.user.UserDomainException;
import com.fastcampus.book_bot.common.exception.user.UserErrorCode;
import com.fastcampus.book_bot.common.utils.JwtUtil;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Transactional
    public User getCurrentUser(HttpServletRequest request) {
        try {
            // Authorization 헤더에서 토큰 추출
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new UserDomainException(UserErrorCode.UNAUTHORIZED_ACCESS.getMessage(),
                        UserErrorCode.UNAUTHORIZED_ACCESS.getCode(), HttpStatus.UNAUTHORIZED);
            }

            String accessToken = authorization.substring(7);

            // 토큰 유효성 검사
            if (jwtUtil.isTokenExpired(accessToken)) {
                throw new UserDomainException(UserErrorCode.UNAUTHORIZED_ACCESS.getMessage(),
                        UserErrorCode.UNAUTHORIZED_ACCESS.getCode(), HttpStatus.UNAUTHORIZED);
            }

            // 토큰에서 사용자 ID 추출
            Integer userId = jwtUtil.extractUserId(accessToken);

            // DB에서 사용자 정보 조회 (등급 정보 포함)
            return (User) userRepository.findByUserId(userId)
                    .orElseThrow(() -> new UserDomainException(UserErrorCode.UNAUTHORIZED_ACCESS.getMessage(),
                            UserErrorCode.UNAUTHORIZED_ACCESS.getCode(), HttpStatus.UNAUTHORIZED));

        } catch (Exception e) {
            log.error("현재 사용자 정보 조회 실패: {}", e.getMessage());
            throw new UserDomainException(UserErrorCode.UNAUTHORIZED_ACCESS.getMessage(),
                    UserErrorCode.UNAUTHORIZED_ACCESS.getCode(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 사용자 ID로 사용자 정보 조회 (내부 서비스용)
     */
    @Transactional
    public User getUserById(Integer userId) {
        return (User) userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserDomainException(UserErrorCode.UNAUTHORIZED_ACCESS.getMessage(),
                        UserErrorCode.UNAUTHORIZED_ACCESS.getCode(), HttpStatus.UNAUTHORIZED));
    }
}
