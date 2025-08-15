package com.fastcampus.book_bot.controller.auth;

import com.fastcampus.book_bot.common.exception.user.UserDomainException;
import com.fastcampus.book_bot.common.exception.user.UserErrorCode;
import com.fastcampus.book_bot.common.response.ApiResponse;
import com.fastcampus.book_bot.common.response.SuccessApiResponse;
import com.fastcampus.book_bot.dto.user.SignupRequestDTO;
import com.fastcampus.book_bot.service.auth.AuthService;
import com.fastcampus.book_bot.service.auth.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
