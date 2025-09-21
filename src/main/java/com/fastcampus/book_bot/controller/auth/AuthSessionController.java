// AuthController.java - Spring Session 버전

package com.fastcampus.book_bot.controller.auth;

import com.fastcampus.book_bot.common.response.SuccessApiResponse;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.user.SignupRequestDTO;
import com.fastcampus.book_bot.dto.user.UserDTO;
import com.fastcampus.book_bot.service.auth.AuthService;
import com.fastcampus.book_bot.service.auth.AuthSessionService;
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
public class AuthSessionController {

    private final MailService mailService;
    private final AuthService authService;
    private final AuthSessionService authSessionService;

    /**
     * 로그인 - Spring Session 사용
     */
    @PostMapping("/login")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> login(
            @RequestBody SignupRequestDTO request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        log.info("=== 로그인 요청 ===");
        log.info("요청 IP: {}", httpRequest.getRemoteAddr());
        log.info("이메일: {}", request.getEmail());

        // 1. 사용자 인증
        User user = authService.login(request);

        // 2. 세션에 사용자 정보 저장
        authSessionService.createUserSession(user, httpRequest);

        // 3. 응답 데이터 구성
        UserDTO userDTO = new UserDTO(user);
        Map<String, Object> data = new HashMap<>();
        data.put("user", userDTO);
        data.put("sessionId", httpRequest.getSession().getId()); // 디버깅용

        log.info("로그인 성공 - 사용자 ID: {}, 닉네임: {}, 세션 ID: {}",
                user.getUserId(), user.getUserNickname(), httpRequest.getSession().getId());

        return ResponseEntity.ok(SuccessApiResponse.of("로그인 완료!", data));
    }

    /**
     * 로그아웃 - 세션 무효화
     */
    @PostMapping("/logout")
    public ResponseEntity<SuccessApiResponse<Void>> logout(HttpServletRequest request) {

        log.info("=== 로그아웃 요청 ===");
        log.info("요청 IP: {}", request.getRemoteAddr());
        log.info("세션 ID: {}", request.getSession(false) != null ?
                request.getSession(false).getId() : "없음");

        // 세션 무효화
        authSessionService.invalidateUserSession(request);

        log.info("로그아웃 완료");
        return ResponseEntity.ok(SuccessApiResponse.of("로그아웃이 완료되었습니다."));
    }

    /**
     * 현재 사용자 정보 조회 (세션 기반)
     */
    @GetMapping("/me")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> getCurrentUser(
            HttpServletRequest request) {

        log.info("=== 현재 사용자 정보 요청 ===");
        log.info("요청 IP: {}", request.getRemoteAddr());

        // 세션에서 사용자 정보 조회
        UserDTO userDTO = authSessionService.getCurrentUser(request);

        Map<String, Object> data = new HashMap<>();
        data.put("user", userDTO);
        data.put("sessionInfo", authSessionService.getSessionInfo(request));

        log.info("현재 사용자 조회 성공 - 사용자 ID: {}", userDTO.getUserId());

        return ResponseEntity.ok(SuccessApiResponse.of("사용자 정보 조회 성공", data));
    }

    /**
     * 세션 연장 (Keep-Alive)
     */
    @PostMapping("/keep-alive")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> keepAlive(
            HttpServletRequest request) {

        log.debug("=== 세션 연장 요청 ===");

        // 세션 유효성 검증 및 연장
        if (!authSessionService.isValidSession(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SuccessApiResponse.of("세션이 만료되었습니다."));
        }

        authSessionService.extendSession(request);

        Map<String, Object> data = new HashMap<>();
        data.put("sessionInfo", authSessionService.getSessionInfo(request));

        return ResponseEntity.ok(SuccessApiResponse.of("세션이 연장되었습니다.", data));
    }

    /**
     * 세션 정보 조회 (디버깅용)
     */
    @GetMapping("/session-info")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> getSessionInfo(
            HttpServletRequest request) {

        log.info("=== 세션 정보 조회 요청 ===");

        Map<String, Object> sessionInfo = authSessionService.getSessionInfo(request);

        return ResponseEntity.ok(SuccessApiResponse.of("세션 정보 조회 성공", sessionInfo));
    }

    // ========== 기존 회원가입 관련 메서드들 (변경 없음) ==========

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SuccessApiResponse<Void>> signup(@RequestBody SignupRequestDTO request) {
        log.info("회원가입 요청 - 이메일: {}", request.getEmail());

        authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessApiResponse.of("회원가입이 완료되었습니다."));
    }

    /**
     * 닉네임 중복 검사
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<SuccessApiResponse<Void>> checkNickname(@RequestParam String nickname) {
        boolean response = authService.isDuplicateNickname(nickname);

        String message = response ? "이미 사용 중인 닉네임입니다" : "사용 가능한 닉네임입니다";
        return ResponseEntity.ok(SuccessApiResponse.of(message));
    }

    /**
     * 이메일 중복 검사
     */
    @GetMapping("/check-email")
    public ResponseEntity<SuccessApiResponse<Void>> checkEmail(@RequestParam String userEmail) {
        boolean response = authService.isDuplicateEmail(userEmail);

        String message = response ? "이미 사용 중인 이메일입니다" : "사용 가능한 이메일입니다";
        return ResponseEntity.ok(SuccessApiResponse.of(message));
    }

    /**
     * 이메일 전송
     */
    @PostMapping("/send-verification")
    public ResponseEntity<SuccessApiResponse<Void>> sendEmail(@RequestParam String userEmail) {
        mailService.sendVerificationCode(userEmail);

        return ResponseEntity.ok(SuccessApiResponse.of("인증코드가 전송되었습니다."));
    }

    /**
     * 이메일 검증
     */
    @PostMapping("/verify-email")
    public ResponseEntity<SuccessApiResponse<Void>> verifyEmail(@RequestBody SignupRequestDTO signupRequest) {
        mailService.verifyCode(signupRequest.getEmail(), signupRequest.getVerificationCode());

        return ResponseEntity.ok(SuccessApiResponse.of("이메일 검증 성공"));
    }
}