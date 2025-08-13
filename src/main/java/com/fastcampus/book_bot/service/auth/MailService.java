package com.fastcampus.book_bot.service.auth;

import com.fastcampus.book_bot.common.response.ApiResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, String> redisTemplaRte;

    private static final String EMAIL_VERIFICATION_PREFIX = "email_verification:";
    private static final int VERIFICATION_EXPIRE_MINUTES = 10;
    private final RedisTemplate<Object, Object> redisTemplate;

    /* 이메일 보내기 */
    private Integer sendMailMessage(String userEmail) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
            messageHelper.setTo(userEmail);
            messageHelper.setSubject("온라인 서점 회원가입 인증번호 안내");

            Context context = new Context();
            Random random = new Random();
            Integer code = random.nextInt(100000, 1000000);
            context.setVariable("verificationCode", code);
            messageHelper.setText(templateEngine.process("auth/email", context), true);

            javaMailSender.send(message);
            return code;

        } catch (Exception e) {
            log.error("메일 전송 실패", e);
            throw new RuntimeException("메일 전송 실패");
        }

    }

    /* redis에 이메일, 인증 번호 저장 */
    public void sendVerificationCode(String userEmail) {

        Integer code = sendMailMessage(userEmail);
        String key = EMAIL_VERIFICATION_PREFIX + userEmail;
        redisTemplaRte.opsForValue().set(key, code.toString(), Duration.ofMinutes(VERIFICATION_EXPIRE_MINUTES));
    }

    /* redis에서 인증 번호 검증 */
    public boolean verifyCode(String userEmail, String inputCode) {
        try {
            String key = EMAIL_VERIFICATION_PREFIX + userEmail;
            String storedCode = redisTemplaRte.opsForValue().get(key);

            if (storedCode != null && storedCode.equals(inputCode)) {
                redisTemplate.delete(key);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("인증 코드 검증 실패: {}", userEmail, e);
            throw new RuntimeException("인증 코드 검증 실패");
        }
    }
}
