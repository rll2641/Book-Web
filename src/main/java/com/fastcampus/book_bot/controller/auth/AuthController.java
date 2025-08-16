package com.fastcampus.book_bot.controller.auth;

import com.fastcampus.book_bot.common.response.SuccessApiResponse;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.user.SignupRequestDTO;
import com.fastcampus.book_bot.service.auth.AuthRedisService;
import com.fastcampus.book_bot.service.auth.AuthService;
import com.fastcampus.book_bot.service.auth.MailService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class AuthController {

    private final MailService mailService;
    private final AuthService authService;
    private final AuthRedisService authRedisService;

    /** 로그인
     * @param request SignupRequestDTO
     * @param response 쿠키 저장
     */
    @PostMapping("/login")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> login(@RequestBody SignupRequestDTO request,
                                                   HttpServletResponse response) {
        User user = authService.login(request);
        String accessToken = authRedisService.setTokenUser(user, response);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("user", Map.of(
                "userId", user.getUserId(),
                "email", user.getUserEmail(),
                "nickname", user.getUserNickname(),
                "name", user.getUserName()
        ));

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
                                                           HttpServletResponse response) {
        authRedisService.deleteTokenUser(authorization, refreshToken, response);

        return ResponseEntity.ok(SuccessApiResponse.of("로그아웃이 완료되었습니다."));
    }

    /**
     * Access Token 갱신
     * @param refreshToken 쿠키의 refreshToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<SuccessApiResponse<Map<String, String>>> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        String newAccessToken = authRedisService.refreshAccessToken(refreshToken);

        Map<String, String> data = new HashMap<>();
        data.put("accessToken", newAccessToken);

        return ResponseEntity.ok(SuccessApiResponse.of("Access Token이 갱신되었습니다.", data));
    }

    /** 회원가입
     * @param request 회원가입 DTO
     */
    @PostMapping("/signup")
    public ResponseEntity<SuccessApiResponse<Void>> signup(@RequestBody SignupRequestDTO request) {
        authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessApiResponse.of("회원가입이 완료되었습니다."));
    }

    /* 닉네임 중복 */
    @GetMapping("/check-nickname")
    public ResponseEntity<SuccessApiResponse<Void>> checkNickname(@RequestParam String nickname) {
        boolean response = authService.isDuplicateNickname(nickname);

        String message = response ? "이미 사용 중인 이메일입니다" : "사용 가능한 이메일입니다";
        return ResponseEntity.ok(SuccessApiResponse.of(message));
    }

    /* 이메일 중복 */
    @GetMapping("/check-email")
    public ResponseEntity<SuccessApiResponse<Void>> checkEmail(@RequestParam String userEmail) {
        boolean response = authService.isDuplicateEmail(userEmail);

        String message = response ? "이미 사용 중인 닉네임입니다" : "사용 가능한 닉네임입니다";
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
