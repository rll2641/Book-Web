package com.fastcampus.book_bot.service.auth;

import com.fastcampus.book_bot.common.exception.user.UserDomainException;
import com.fastcampus.book_bot.common.exception.user.UserErrorCode;
import com.fastcampus.book_bot.common.utils.EncoderUtils;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.user.SignupRequestDTO;
import com.fastcampus.book_bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EncoderUtils encoderUtils;

    /**
     * 닉네임 중복 검증
     */
    @Transactional(readOnly = true)
    public boolean isDuplicateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw UserDomainException.badRequest(
                    UserErrorCode.INVALID_DATA.getMessage(),
                    UserErrorCode.INVALID_DATA.getCode()
            );
        }

        try {
            return userRepository.existsByUserNickname(nickname);
        } catch (Exception e) {
            log.error("닉네임 중복 검증 실패: {}", nickname, e);
            throw UserDomainException.internalServerError(
                    UserErrorCode.SYSTEM_ERROR.getMessage(),
                    UserErrorCode.SYSTEM_ERROR.getCode(),
                    Map.of("field", "nickname", "value", nickname)
            );
        }
    }

    /**
     * 이메일 중복 검증
     */
    @Transactional(readOnly = true)
    public boolean isDuplicateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw UserDomainException.badRequest(
                    UserErrorCode.INVALID_DATA.getMessage(),
                    UserErrorCode.INVALID_DATA.getCode()
            );
        }

        try {
            return userRepository.existsByUserEmail(email);
        } catch (Exception e) {
            log.error("이메일 중복 검증 실패: {}", email, e);
            throw UserDomainException.internalServerError(
                    UserErrorCode.SYSTEM_ERROR.getMessage(),
                    UserErrorCode.SYSTEM_ERROR.getCode(),
                    Map.of("field", "email", "value", email)
            );
        }
    }

    /**
     * 회원가입 처리
     */
    @Transactional
    public void signup(SignupRequestDTO request) {

        try {
            if (isDuplicateEmail(request.getEmail())) {
                throw UserDomainException.conflict(
                        "이미 존재하는 이메일입니다: " + request.getEmail(),
                        UserErrorCode.EMAIL_ALREADY_EXISTS.getCode(),
                        Map.of("email", request.getEmail())
                );
            }

            if (isDuplicateNickname(request.getNickname())) {
                throw UserDomainException.conflict(
                        "이미 존재하는 닉네임입니다: " + request.getNickname(),
                        UserErrorCode.NICKNAME_ALREADY_EXISTS.getCode(),
                        Map.of("nickname", request.getNickname())
                );
            }

            User user = User.builder()
                    .userEmail(request.getEmail())
                    .userPassword(encoderUtils.passwordEncode(request.getPassword()))
                    .userName(request.getName())
                    .userNickname(request.getNickname())
                    .userPhone(request.getPhone())
                    .userStatus("ACTIVE")
                    .build();

            userRepository.save(user);
            log.info("회원가입 완료: {}", request.getEmail());

        } catch (UserDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생: {}", request.getEmail(), e);
            throw UserDomainException.internalServerError(
                    UserErrorCode.SYSTEM_ERROR.getMessage(),
                    UserErrorCode.SYSTEM_ERROR.getCode(),
                    Map.of("email", request.getEmail())
            );
        }
    }

    @Transactional(readOnly = true)
    public User login(SignupRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw UserDomainException.badRequest(
                    "이메일은 필수입니다",
                    UserErrorCode.INVALID_DATA.getCode()
            );
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw UserDomainException.badRequest(
                    "비밀번호는 필수입니다",
                    UserErrorCode.INVALID_DATA.getCode()
            );
        }

        try {
            User user = userRepository.findByUserEmail(request.getEmail());

            if (user == null) {
                throw UserDomainException.unauthorized(
                        UserErrorCode.INVALID_CREDENTIALS.getMessage(),
                        UserErrorCode.INVALID_CREDENTIALS.getCode(),
                        Map.of("email", request.getEmail())
                );
            }

            if (!encoderUtils.validatePassword(request.getPassword(), user.getUserPassword())) {
                throw UserDomainException.unauthorized(
                        UserErrorCode.INVALID_CREDENTIALS.getMessage(),
                        UserErrorCode.INVALID_CREDENTIALS.getCode(),
                        Map.of("email", request.getEmail())
                );
            }

            if (!"ACTIVE".equals(user.getUserStatus())) {
                throw UserDomainException.forbidden(
                        UserErrorCode.ACCOUNT_INACTIVE.getMessage(),
                        UserErrorCode.ACCOUNT_INACTIVE.getCode(),
                        Map.of("status", user.getUserStatus(), "email", request.getEmail())
                );
            }

            log.info("로그인 성공: {}", request.getEmail());
            return user;

        } catch (UserDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: {}", request.getEmail(), e);
            throw UserDomainException.internalServerError(
                    UserErrorCode.SYSTEM_ERROR.getMessage(),
                    UserErrorCode.SYSTEM_ERROR.getCode(),
                    Map.of("email", request.getEmail())
            );
        }
    }

}
