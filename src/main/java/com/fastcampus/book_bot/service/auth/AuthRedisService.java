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

            log.info("Redis에 Refresh Token 저장 - Key: {}, 만료시간: 7일", redisKey);

            // 쿠키 설정 개선
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);    // XSS 공격 방지
            refreshCookie.setSecure(false);     // 개발환경: false, 운영환경: true
            refreshCookie.setPath("/");         // 모든 경로에서 접근 가능
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일

            // SameSite 속성을 포함한 쿠키 헤더 직접 설정
            response.setHeader("Set-Cookie",
                    String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
                            "refreshToken", refreshToken, 7 * 24 * 60 * 60));

            log.info("로그인 성공 - 사용자 ID: {}, Refresh Token 쿠키 설정 완료", user.getUserId());
            return accessToken;
        } catch (Exception e) {
            log.error("토큰 생성 중 오류 발생: ", e);
            throw UserDomainException.badRequest(UserErrorCode.LOGIN_FAILED.getMessage(), UserErrorCode.LOGIN_FAILED.getCode());
        }

    }

    /** 로그아웃시, JWT 삭제
     * */
    public void deleteTokenUser(String authorization, String refreshToken, HttpServletResponse response) {
        try {
            log.info("=== 로그아웃 처리 시작 ===");

            // Authorization 헤더에서 Access Token 추출
            String accessToken = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                accessToken = authorization.substring(7);
                log.info("Access Token 추출됨");
            }

            // Access Token 기본 유효성 검사 후 사용자 ID 추출
            Integer userId = null;
            if (accessToken != null) {
                try {
                    if (!jwtUtil.isTokenExpired(accessToken)) {
                        userId = jwtUtil.extractUserId(accessToken);
                        log.info("Access Token에서 사용자 ID 추출: {}", userId);
                    }
                } catch (Exception e) {
                    log.warn("Access Token 파싱 실패: {}", e.getMessage());
                }
            }

            // Refresh Token에서도 사용자 ID 추출 시도
            if (userId == null && refreshToken != null) {
                try {
                    if (!jwtUtil.isTokenExpired(refreshToken)) {
                        userId = jwtUtil.extractUserId(refreshToken);
                        log.info("Refresh Token에서 사용자 ID 추출: {}", userId);
                    }
                } catch (Exception e) {
                    log.warn("Refresh Token 파싱 실패: {}", e.getMessage());
                }
            }

            // Redis에서 Refresh Token 삭제
            if (userId != null) {
                String redisKey = "refresh_token:" + userId;
                Boolean deleted = redisTemplate.delete(redisKey);
                log.info("Redis에서 사용자 토큰 삭제 - ID: {}, 삭제 성공: {}", userId, deleted);
            }

            // HttpOnly 쿠키에서 Refresh Token 삭제
            Cookie deleteCookie = new Cookie("refreshToken", null);
            deleteCookie.setHttpOnly(true);
            deleteCookie.setSecure(false); // 개발환경에 맞게 설정
            deleteCookie.setPath("/");
            deleteCookie.setMaxAge(0);  // 즉시 만료
            response.addCookie(deleteCookie);

            // 추가적으로 Set-Cookie 헤더로도 삭제
            response.setHeader("Set-Cookie",
                    "refreshToken=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");

            log.info("쿠키에서 Refresh Token 삭제 완료");
            log.info("=== 로그아웃 처리 완료 ===");

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: ", e);
            throw UserDomainException.internalServerError(UserErrorCode.SYSTEM_ERROR.getMessage(), UserErrorCode.SYSTEM_ERROR.getCode());
        }
    }

    /** AccessToken 갱신 (개선된 버전)
     * */
    public String refreshAccessToken(String refreshToken) {
        try {
            log.info("=== Refresh Token 갱신 시작 ===");
            log.info("Refresh Token 존재 여부: {}", refreshToken != null ? "존재" : "null");

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                log.warn("Refresh Token이 null이거나 비어있음");
                throw UserDomainException.badRequest("RefreshToken이 없습니다.", UserErrorCode.INVALID_DATA.getCode());
            }

            // 토큰 만료 확인
            if (jwtUtil.isTokenExpired(refreshToken)) {
                log.warn("만료된 Refresh Token");
                throw UserDomainException.badRequest("만료된 RefreshToken입니다.", UserErrorCode.INVALID_DATA.getCode());
            }

            // 토큰에서 사용자 ID 추출
            Integer userId;
            try {
                userId = jwtUtil.extractUserId(refreshToken);
                log.info("Refresh Token에서 사용자 ID 추출: {}", userId);
            } catch (Exception e) {
                log.error("Refresh Token 파싱 실패: ", e);
                throw UserDomainException.badRequest("유효하지 않은 Refresh Token 형식입니다.", UserErrorCode.INVALID_DATA.getCode());
            }

            // Redis에서 저장된 토큰 조회
            String redisKey = "refresh_token:" + userId;
            String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);

            log.info("Redis 조회 - Key: {}, 저장된 토큰: {}", redisKey, storedRefreshToken != null ? "존재" : "없음");

            if (storedRefreshToken == null) {
                log.warn("Redis에 저장된 Refresh Token이 없음 - 사용자 ID: {}", userId);
                throw UserDomainException.badRequest("세션이 만료되었습니다. 다시 로그인해주세요.", UserErrorCode.INVALID_DATA.getCode());
            }

            if (!storedRefreshToken.equals(refreshToken)) {
                log.warn("Refresh Token 불일치 - 사용자 ID: {}", userId);
                log.debug("요청된 토큰 앞 10자리: {}", refreshToken.substring(0, Math.min(10, refreshToken.length())));
                log.debug("저장된 토큰 앞 10자리: {}", storedRefreshToken.substring(0, Math.min(10, storedRefreshToken.length())));
                throw UserDomainException.badRequest("유효하지 않은 Refresh Token입니다.", UserErrorCode.INVALID_DATA.getCode());
            }

            // 새로운 Access Token 생성
            String newAccessToken = jwtUtil.createAccessToken(userId, "USER");

            log.info("Access Token 갱신 성공 - 사용자 ID: {}", userId);
            log.info("=== Refresh Token 갱신 완료 ===");
            return newAccessToken;

        } catch (UserDomainException e) {
            log.error("도메인 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("토큰 갱신 중 예상치 못한 오류 발생: ", e);
            throw UserDomainException.internalServerError("토큰 갱신 중 오류 발생!", UserErrorCode.SYSTEM_ERROR.getCode());
        }
    }
}
