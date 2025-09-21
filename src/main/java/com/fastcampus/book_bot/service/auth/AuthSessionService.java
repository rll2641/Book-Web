package com.fastcampus.book_bot.service.auth;

import com.fastcampus.book_bot.common.exception.user.UserDomainException;
import com.fastcampus.book_bot.common.exception.user.UserErrorCode;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.user.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthSessionService {

    private static final String USER_SESSION_KEY = "USER_INFO";
    private static final String LOGIN_TIME_KEY = "LOGIN_TIME";

    /**
     * 로그인 성공 시 세션에 사용자 정보 저장
     * */
    public void createUserSession(User user, HttpServletRequest request) {
        try {
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                log.info("기존 세션 무효화 - 사용자 ID: {}", user.getUserId());
                existingSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            UserDTO userDTO = new UserDTO(user);

            session.setAttribute(USER_SESSION_KEY, userDTO);
            session.setAttribute(LOGIN_TIME_KEY, LocalDateTime.now());

            session.getMaxInactiveInterval();
        } catch (Exception e) {
            log.error("세션 생성 중 오류 발생: ", e);
            throw UserDomainException.internalServerError(
                    UserErrorCode.SYSTEM_ERROR.getMessage(),
                    UserErrorCode.SYSTEM_ERROR.getCode()
            );
        }
    }

    /**
     * 현재 세션에서 사용자 정보 조회
     */
    public UserDTO getCurrentUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);

            if (session == null) {
                log.warn("세션이 존재하지 않음");
                throw UserDomainException.unauthorized(
                        "로그인이 필요합니다.",
                        UserErrorCode.UNAUTHORIZED_ACCESS.getCode()
                );
            }

            UserDTO userDTO = (UserDTO) session.getAttribute(USER_SESSION_KEY);

            if (userDTO == null) {
                log.warn("세션에 사용자 정보가 없음 - 세션 ID: {}", session.getId());
                throw UserDomainException.unauthorized(
                        "세션이 만료되었습니다. 다시 로그인해주세요.",
                        UserErrorCode.UNAUTHORIZED_ACCESS.getCode()
                );
            }

            // 세션 활성화 시간 업데이트 (마지막 접근 시간)
            session.setAttribute("LAST_ACCESS_TIME", LocalDateTime.now());

            log.debug("현재 사용자 조회 성공 - 세션 ID: {}, 사용자 ID: {}",
                    session.getId(), userDTO.getUserId());

            return userDTO;

        } catch (UserDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생: ", e);
            throw UserDomainException.internalServerError(
                    UserErrorCode.SYSTEM_ERROR.getMessage(),
                    UserErrorCode.SYSTEM_ERROR.getCode()
            );
        }
    }

    /**
     * 로그아웃 - 세션 무효화
     */
    public void invalidateUserSession(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);

            if (session != null) {
                UserDTO userDTO = (UserDTO) session.getAttribute(USER_SESSION_KEY);
                String sessionId = session.getId();

                session.invalidate();

                log.info("로그아웃 완료 - 세션 ID: {}, 사용자 ID: {}",
                        sessionId, userDTO != null ? userDTO.getUserId() : "unknown");
            } else {
                log.warn("무효화할 세션이 존재하지 않음");
            }

        } catch (Exception e) {
            log.error("세션 무효화 중 오류 발생: ", e);
            throw UserDomainException.internalServerError(
                    UserErrorCode.SYSTEM_ERROR.getMessage(),
                    UserErrorCode.SYSTEM_ERROR.getCode()
            );
        }
    }

    /**
     * 세션 유효성 검증
     */
    public boolean isValidSession(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);

            if (session == null) {
                return false;
            }

            UserDTO userDTO = (UserDTO) session.getAttribute(USER_SESSION_KEY);
            return userDTO != null;

        } catch (Exception e) {
            log.error("세션 유효성 검증 중 오류 발생: ", e);
            return false;
        }
    }

    /**
     * 세션 연장 (활동이 있을 때 세션 유지)
     */
    public void extendSession(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);

            if (session != null && isValidSession(request)) {
                // 마지막 접근 시간 업데이트
                session.setAttribute("LAST_ACCESS_TIME", LocalDateTime.now());
                log.debug("세션 연장 완료 - 세션 ID: {}", session.getId());
            }

        } catch (Exception e) {
            log.error("세션 연장 중 오류 발생: ", e);
        }
    }

    /**
     * 세션 정보 조회 (디버깅용)
     */
    public Map<String, Object> getSessionInfo(HttpServletRequest request) {
        Map<String, Object> sessionInfo = new HashMap<>();

        try {
            HttpSession session = request.getSession(false);

            if (session != null) {
                sessionInfo.put("sessionId", session.getId());
                sessionInfo.put("creationTime", session.getCreationTime());
                sessionInfo.put("lastAccessedTime", session.getLastAccessedTime());
                sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
                sessionInfo.put("isNew", session.isNew());

                UserDTO userDTO = (UserDTO) session.getAttribute(USER_SESSION_KEY);
                if (userDTO != null) {
                    sessionInfo.put("userId", userDTO.getUserId());
                    sessionInfo.put("userNickname", userDTO.getUserNickname());
                }

                LocalDateTime loginTime = (LocalDateTime) session.getAttribute(LOGIN_TIME_KEY);
                if (loginTime != null) {
                    sessionInfo.put("loginTime", loginTime);
                }
            } else {
                sessionInfo.put("error", "세션이 존재하지 않습니다.");
            }

        } catch (Exception e) {
            log.error("세션 정보 조회 중 오류 발생: ", e);
            sessionInfo.put("error", "세션 정보 조회 실패: " + e.getMessage());
        }

        return sessionInfo;
    }
}
