package com.fastcampus.book_bot.controller.auth;

import com.fastcampus.book_bot.common.response.SuccessApiResponse;
import com.fastcampus.book_bot.common.utils.JwtUtil;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.user.SignupRequestDTO;
import com.fastcampus.book_bot.dto.user.UserDTO;
import com.fastcampus.book_bot.service.auth.AuthRedisService;
import com.fastcampus.book_bot.service.auth.AuthService;
import com.fastcampus.book_bot.service.auth.MailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final MailService mailService;
    private final AuthService authService;
    private final AuthRedisService authRedisService;
    private final JwtUtil jwtUtil; // JWT 유틸리티 추가

    /** 로그인
     * @param request SignupRequestDTO
     * @param response 쿠키 저장
     */
    @PostMapping("/login")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> login(@RequestBody SignupRequestDTO request,
                                                                         HttpServletResponse response,
                                                                         HttpServletRequest httpRequest) {

        log.info("=== 로그인 요청 ===");
        log.info("요청 IP: {}", httpRequest.getRemoteAddr());
        log.info("이메일: {}", request.getEmail());

        User user = authService.login(request);
        String accessToken = authRedisService.setTokenUser(user, response);

        UserDTO userDTO = new UserDTO(user);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("user", userDTO);

        log.info("로그인 성공 - 사용자 ID: {}, 닉네임: {}", user.getUserId(), user.getUserNickname());

        return ResponseEntity.ok(SuccessApiResponse.of("로그인 완료!", data));
    }

    /**
     * 로그아웃
     * @param authorization Authorization 헤더
     * @param refreshToken 쿠키의 refreshToken
     * @param response HttpServletResponse
     */
    @PostMapping("/logout")
    public ResponseEntity<SuccessApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                           @CookieValue(value = "refreshToken", required = false) String refreshToken,
                                                           HttpServletResponse response,
                                                           HttpServletRequest request) {

        log.info("=== 로그아웃 요청 ===");
        log.info("요청 IP: {}", request.getRemoteAddr());
        log.info("Authorization 헤더: {}", authorization != null ? "존재" : "없음");
        log.info("RefreshToken 쿠키: {}", refreshToken != null ? "존재" : "없음");

        authRedisService.deleteTokenUser(authorization, refreshToken, response);

        return ResponseEntity.ok(SuccessApiResponse.of("로그아웃이 완료되었습니다."));
    }

    /**
     * Access Token 갱신 (개선된 버전)
     * @param refreshToken 쿠키의 refreshToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletRequest request) {

        log.info("=== Refresh Token 요청 ===");
        log.info("요청 IP: {}", request.getRemoteAddr());
        log.info("User-Agent: {}", request.getHeader("User-Agent"));
        log.info("쿠키에서 받은 refreshToken: {}", refreshToken != null ? "존재" : "null");

        // 요청 헤더의 모든 쿠키 로깅
        String cookieHeader = request.getHeader("Cookie");
        log.info("Cookie 헤더: {}", cookieHeader != null ? cookieHeader : "없음");

        if (refreshToken != null) {
            log.info("refreshToken 길이: {}", refreshToken.length());
            log.info("refreshToken 앞 10자리: {}", refreshToken.substring(0, Math.min(10, refreshToken.length())));
        }

        String newAccessToken = authRedisService.refreshAccessToken(refreshToken);

        // 사용자 정보도 함께 반환하도록 수정
        Integer userId = jwtUtil.extractUserId(newAccessToken);

        // 기본 사용자 정보 (실제 구현에서는 UserService에서 조회)
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("nickname", "사용자"); // 기본값, 실제로는 DB에서 조회

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", newAccessToken);
        data.put("user", userInfo); // 사용자 정보 포함

        log.info("Refresh Token 갱신 완료 - 사용자 ID: {}", userId);

        return ResponseEntity.ok(SuccessApiResponse.of("Access Token이 갱신되었습니다.", data));
    }

    /** 회원가입
     * @param request 회원가입 DTO
     */
    @PostMapping("/signup")
    public ResponseEntity<SuccessApiResponse<Void>> signup(@RequestBody SignupRequestDTO request) {
        log.info("회원가입 요청 - 이메일: {}", request.getEmail());

        authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessApiResponse.of("회원가입이 완료되었습니다."));
    }

    /* 닉네임 중복 */
    @GetMapping("/check-nickname")
    public ResponseEntity<SuccessApiResponse<Void>> checkNickname(@RequestParam String nickname) {
        boolean response = authService.isDuplicateNickname(nickname);

        String message = response ? "이미 사용 중인 닉네임입니다" : "사용 가능한 닉네임입니다";
        return ResponseEntity.ok(SuccessApiResponse.of(message));
    }

    /* 이메일 중복 */
    @GetMapping("/check-email")
    public ResponseEntity<SuccessApiResponse<Void>> checkEmail(@RequestParam String userEmail) {
        boolean response = authService.isDuplicateEmail(userEmail);

        String message = response ? "이미 사용 중인 이메일입니다" : "사용 가능한 이메일입니다";
        return ResponseEntity.ok(SuccessApiResponse.of(message));
    }

    /* 이메일 전송 */
    @PostMapping("/send-verification")
    public ResponseEntity<SuccessApiResponse<Void>> sendEmail(@RequestParam String userEmail) {
        mailService.sendVerificationCode(userEmail);

        return ResponseEntity.ok(SuccessApiResponse.of("인증코드가 전송되었습니다."));
    }

    /* 이메일 검증 */
    @PostMapping("/verify-email")
    public ResponseEntity<SuccessApiResponse<Void>> verifyEmail(@RequestBody SignupRequestDTO signupRequest) {
        mailService.verifyCode(signupRequest.getEmail(), signupRequest.getVerificationCode());

        return ResponseEntity.ok(SuccessApiResponse.of("이메일 검증 성공"));
    }
}
