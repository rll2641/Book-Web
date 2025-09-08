package com.fastcampus.book_bot.service.auth;

import com.fastcampus.book_bot.common.exception.user.UserDomainException;
import com.fastcampus.book_bot.common.exception.user.UserErrorCode;
import com.fastcampus.book_bot.common.utils.JwtUtil;
import com.fastcampus.book_bot.domain.user.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthRedisService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    /** 로그인 시, JWT 토큰 발급
     * */
    public String setTokenUser(User user, HttpServletResponse response) {

        try {
            // Access Token 생성
            String accessToken = jwtUtil.createAccessToken(user.getUserId(), "USER");

            // Refresh Token 생성
            String refreshToken = jwtUtil.createRefreshToken(user.getUserId());

            // redis에 Refresh Token 저장
            String redisKey = "refresh_token:" + user.getUserId();
            redisTemplate.opsForValue().set(redisKey, refreshToken, 7, TimeUnit.DAYS);

            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);  // XSS 공격 방지
            refreshCookie.setSecure(false);    // HTTP에서만 전송
            refreshCookie.setPath("/");       // 모든 경로에서 접근 가능
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);

            response.addCookie(refreshCookie);

            log.info("로그인 성공 - 사용자 ID: {}", user.getUserId());
            return accessToken;
        } catch (Exception e) {
            throw UserDomainException.badRequest(UserErrorCode.LOGIN_FAILED.getMessage(), UserErrorCode.LOGIN_FAILED.getCode());
        }

    }

    /** 로그아웃시, JWT 삭제
     * */
    public void deleteTokenUser(String authorization, String refreshToken, HttpServletResponse response) {
        try {
            // Authorization 헤더에서 Access Token 추출
            String accessToken = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                accessToken = authorization.substring(7);
            }

            // Access Token 기본 유효성 검사 후 사용자 ID 추출
            Integer userId = null;
            if (accessToken != null) {
                try {
                    if (!jwtUtil.isTokenExpired(accessToken)) {
                        userId = jwtUtil.extractUserId(accessToken);
                    }
                } catch (Exception e) {
                    log.warn("Access Token 파싱 실패: {}", e.getMessage());
                }
            }

            // Redis에서 Refresh Token 삭제
            if (userId != null) {
                String redisKey = "refresh_token:" + userId;
                redisTemplate.delete(redisKey);
                log.info("Redis에서 사용자 삭제 ID: {}", userId);
            }

            // 4. HttpOnly 쿠키에서 Refresh Token 삭제
            if (refreshToken != null) {
                Cookie deleteCookie = new Cookie("refreshToken", null);
                deleteCookie.setHttpOnly(true);
                deleteCookie.setSecure(true);
                deleteCookie.setPath("/");
                deleteCookie.setMaxAge(0);  // 즉시 만료
                response.addCookie(deleteCookie);
                log.info("쿠키에서 Refresh Token 삭제");
            }
        } catch (Exception e) {
            throw UserDomainException.internalServerError(UserErrorCode.SYSTEM_ERROR.getMessage(), UserErrorCode.SYSTEM_ERROR.getCode());
        }
    }

    /** AccessToken 갱신
     * */
    public String refreshAccessToken(String refreshToken) {
        try {
            if (refreshToken == null) {
                throw UserDomainException.badRequest("RefreshToken이 없습니다.", UserErrorCode.INVALID_DATA.getCode());
            }

            if (jwtUtil.isTokenExpired(refreshToken)) {
                throw UserDomainException.badRequest("만료된 RefreshToken입니다.", UserErrorCode.INVALID_DATA.getCode());
            }

            Integer userId = jwtUtil.extractUserId(refreshToken);

            String redisKey = "refresh_token:" + userId;
            String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);

            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                throw UserDomainException.badRequest("유효하지 않은 Refresh Token입니다.", UserErrorCode.INVALID_DATA.getCode());
            }

            String newAccessToken = jwtUtil.createAccessToken(userId, "USER");

            log.info("Access Token 갱신 성공 - 사용자 ID: {}", userId);
            return newAccessToken;
        } catch (UserDomainException e) {
            throw e;
        } catch (Exception e) {
            throw UserDomainException.internalServerError("토큰 갱신 중 오류 발생!", UserErrorCode.SYSTEM_ERROR.getCode());
        }
    }
}
