package com.fastcampus.book_bot.common.intercept;

import com.fastcampus.book_bot.common.utils.JwtUtil;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.service.auth.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                response.sendRedirect("/login");
                return false;
            }

            String accessToken = authorization.substring(7);

            if (jwtUtil.isTokenExpired(accessToken)) {
                response.sendRedirect("/login");
                return false;
            }

            Integer userId = jwtUtil.extractUserId(accessToken);

            User currentUser = userService.getUserById(userId);

            request.setAttribute("currentUser", currentUser);

            return true;
        } catch (Exception e) {
            log.error("interceptor: 인증 실패: {}", e.getMessage());
            try {
                response.sendRedirect("/login");
            } catch (IOException ioException) {
                log.error("리다이렉트 실패", ioException);
            }
            return false;
        }
    }
}
