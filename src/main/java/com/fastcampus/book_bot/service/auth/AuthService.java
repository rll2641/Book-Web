package com.fastcampus.book_bot.service.auth;

import com.fastcampus.book_bot.common.utils.EncoderUtils;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.user.SignupRequestDTO;
import com.fastcampus.book_bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EncoderUtils encoderUtils;

    /* 닉네임 중복 검증 */
    @Transactional(readOnly = true)
    public boolean isDuplicateNickname(String nickname) {
        try {
            return userRepository.existsByUserNickname(nickname);

        } catch (Exception e) {
            log.error("닉네임 중복 검증 실패", e);
            throw new RuntimeException("닉네임 중복 확인 중 오류 발생", e);
        }
    }

    /* 이메일 중복 검증 */
    @Transactional(readOnly = true)
    public boolean isDuplicateEmail(String email) {
        try {
            return userRepository.existsByUserEmail(email);

        } catch (Exception e) {
            log.error("이메일 중복 검증 실패");
            throw new RuntimeException("이메일 중복 확인 중 오류 발생", e);
        }
    }

    /* 회원 가입 */
    @Transactional
    public void signup(SignupRequestDTO request) {

        try {
            if (isDuplicateNickname(request.getNickname())) {
                throw new RuntimeException("이미 사용중인 이메일입니다.");
            }

            if (isDuplicateNickname(request.getNickname())) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }

            if (!request.isAgreeTerms()) {
                throw new RuntimeException("약관에 동의해야 합니다.");
            }

            if (!request.isEmailVerified()) {
                throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
            }

            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
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

        } catch (Exception e) {
            throw new RuntimeException("회원가입 중 오류 발생!", e);
        }
    }

    @Transactional(readOnly = true)
    public void login(SignupRequestDTO request) {

        try {
            User user = userRepository.findByUserEmail(request.getEmail());

            if (user == null) {
                throw new RuntimeException("이메일 또는 비밀번호를 확인해주세요");
            }

            if (!encoderUtils.validatePassword(request.getPassword(), user.getUserPassword())) {
                throw new RuntimeException("이메일 또는 비밀번호를 확인해주세요");
            }

        } catch (Exception e) {
            throw new RuntimeException("로그인 중 오류 발생!", e);
        }
    }

}
