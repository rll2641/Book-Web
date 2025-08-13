package com.fastcampus.book_bot.controller.auth;

import com.fastcampus.book_bot.common.response.ApiResponse;
import com.fastcampus.book_bot.dto.user.SignupRequestDTO;
import com.fastcampus.book_bot.service.auth.AuthService;
import com.fastcampus.book_bot.service.auth.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class AuthController {

    private final MailService mailService;
    private final AuthService authService;

    /* 로그인 */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody SignupRequestDTO request) {
        authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("로그인 완료!"));
    }

    /* 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody SignupRequestDTO request) {
        authService.signup(request);

        return ResponseEntity.ok(ApiResponse.success("회원가입 완료!"));
    }

    /* 닉네임 중복 */
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Void>> checkNickname(@RequestParam String nickname) {
        boolean response = authService.isDuplicateNickname(nickname);

        if (response) {
            return ResponseEntity.ok(ApiResponse.success("중복된 닉네임입니다."));
        } else {
            return ResponseEntity.ok(ApiResponse.success("가능한!"));
        }
    }

    /* 이메일 중복 */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Void>> checkEmail(@RequestParam String userEmail) {
        boolean response = authService.isDuplicateEmail(userEmail);

        if (response) {
            return ResponseEntity.ok(ApiResponse.success("중복된 이메일입니다."));
        } else {
            return ResponseEntity.ok(ApiResponse.success("가능한!"));
        }
    }

    /* 이메일 전송 */
    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<Void>> sendEmail(@RequestParam String userEmail) {
        mailService.sendVerificationCode(userEmail);

        return ResponseEntity.ok(ApiResponse.success("이메일 전송이 완료되었습니다."));
    }

    /* 이메일 검증 */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody SignupRequestDTO signupRequest) {
        boolean response = mailService.verifyCode(signupRequest.getEmail(), signupRequest.getVerificationCode());

        if (response) {
            return ResponseEntity.ok(ApiResponse.success("이메일 검증 성공"));
        } else {
            return ResponseEntity.ok(ApiResponse.success("인증번호가 틀립니다"));
        }
    }
}
