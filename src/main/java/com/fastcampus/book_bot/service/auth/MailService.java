package com.fastcampus.book_bot.service.auth;

import com.fastcampus.book_bot.common.exception.user.UserDomainException;
import com.fastcampus.book_bot.common.exception.user.UserErrorCode;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String EMAIL_VERIFICATION_PREFIX = "email_verification:";
    private static final int VERIFICATION_EXPIRE_MINUTES = 10;

    /** 이메일 보내기
     * @param userEmail 사용자 이메일
     * */
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
            log.error("메일 전송 실패: {}", userEmail, e);
            throw UserDomainException.unprocessableEntity(
                    UserErrorCode.EMAIL_SEND_FAILED.getMessage(),
                    UserErrorCode.EMAIL_SEND_FAILED.getCode(),
                    Map.of("email", userEmail)
            );
        }
    }

    /** redis에 이메일, 인증 번호 저장
     * @param userEmail 사용자 이메일
     * */
    public void sendVerificationCode(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw UserDomainException.badRequest(
                    UserErrorCode.INVALID_DATA.getMessage(),
                    UserErrorCode.INVALID_DATA.getCode()
            );
        }

        try {
            Integer code = sendMailMessage(userEmail);
            String key = EMAIL_VERIFICATION_PREFIX + userEmail;
            redisTemplate.opsForValue().set(key, code.toString(), Duration.ofMinutes(VERIFICATION_EXPIRE_MINUTES));
        } catch (UserDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("인증코드 전송 실패: {}", userEmail, e);
            throw UserDomainException.internalServerError(
                    UserErrorCode.SYSTEM_ERROR.getMessage(),
                    UserErrorCode.SYSTEM_ERROR.getCode(),
                    Map.of("email", userEmail)
            );
        }
    }

    /** redis 인증 번호 검증
     * @param userEmail 사용자 이메일
     * @param inputCode 인증 번호
     *  */
    public boolean verifyCode(String userEmail, String inputCode) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw UserDomainException.badRequest(
                    UserErrorCode.INVALID_DATA.getMessage(),
                    UserErrorCode.INVALID_DATA.getCode()
            );
        }

        if (inputCode == null || inputCode.trim().isEmpty()) {
            throw UserDomainException.badRequest(
                    UserErrorCode.INVALID_DATA.getMessage(),
                    UserErrorCode.INVALID_DATA.getCode()
            );
        }

        try {
            String key = EMAIL_VERIFICATION_PREFIX + userEmail;
            String storedCode = redisTemplate.opsForValue().get(key);

            if (storedCode != null && storedCode.equals(inputCode)) {
                redisTemplate.delete(key);
                return true;
            } else {
                throw UserDomainException.badRequest(UserErrorCode.VERIFICATION_CODE_INVALID.getMessage(),
                        UserErrorCode.VERIFICATION_CODE_INVALID.getCode());
            }
        } catch (Exception e) {
            log.error("인증 코드 검증 실패: {}", userEmail, e);
            throw UserDomainException.internalServerError(
                    UserErrorCode.SYSTEM_ERROR.getMessage(),
                    UserErrorCode.SYSTEM_ERROR.getCode(),
                    Map.of("email", userEmail)
            );
        }
    }

    /**
     * 재고 알림 이메일 발송
     * @param userEmail 사용자 이메일
     * @param bookTitle 도서 제목
     * @param currentStock 현재 재고
     * @param message 알림 메시지
     */
    public void sendStockNotification(String userEmail, String bookTitle, int currentStock, String message) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setTo(userEmail);
            messageHelper.setSubject("📚 재고 알림 - " + bookTitle);

            Context context = new Context();
            context.setVariable("bookTitle", bookTitle);
            context.setVariable("currentStock", currentStock);
            context.setVariable("message", message);
            context.setVariable("notificationTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")));

            String htmlContent = templateEngine.process("notification/stock-alert", context);
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            log.info("재고 알림 이메일 발송 성공 - 수신자: {}, 도서: {}, 재고: {}권", userEmail, bookTitle, currentStock);

        } catch (Exception e) {
            log.error("재고 알림 이메일 발송 실패 - 수신자: {}, 도서: {}", userEmail, bookTitle, e);
            throw new RuntimeException("재고 알림 이메일 발송에 실패했습니다.", e);
        }
    }
}