package com.fastcampus.book_bot.common.intercept;

import com.fastcampus.book_bot.service.auth.AuthSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final AuthSessionService authSessionService;
    private final ObjectMapper objectMapper;

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/member/login",
            "/api/member/signup",
            "/api/member/check-nickname",
            "/api/member/check-email",
            "/api/member/send-verification",
            "/api/member/verify-email",
            "/login",
            "/signup"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equals(method)) {
            return true;
        }

        if (isExcludePath(requestURI)) {
            log.debug("인증 제외 경로: {}", requestURI);
            return true;
        }

        try {
            if (!authSessionService.isValidSession(request)) {
                log.warn("유효하지 않은 세션 - URI: {}, IP: {}", requestURI, request.getRemoteAddr());
                return false;
            }

            // 세션 연장 (활동이 있으므로)
            authSessionService.extendSession(request);

            log.debug("인증 성공 - URI: {}", requestURI);
            return true;

        } catch (Exception e) {
            log.error("인증 처리 중 오류 발생: ", e);
            return false;
        }
    }

    /**
     * 제외 경로 확인
     * */
    private boolean isExcludePath(String requestURI) {
        return EXCLUDE_PATHS.stream()
                .anyMatch(pattern -> {
                    if (pattern.endsWith("/**")) {
                        return requestURI.startsWith(pattern.substring(0, pattern.length() - 3));
                    }
                    return requestURI.equals(pattern);
                });
    }
}
