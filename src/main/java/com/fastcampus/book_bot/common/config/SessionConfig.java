package com.fastcampus.book_bot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class SessionConfig {

    /**
     * 세션 쿠키 설정
     * - HttpOnly : XSS 공격 방지
     * - Secure : HTTPS에서만 전송
     * - SameSite : CSRF 공격 방지
     * */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setCookiePath("/");
        serializer.setDomainNamePattern("^.+$");
        serializer.setUseHttpOnlyCookie(false);
        serializer.setUseSecureCookie(false);
        serializer.setSameSite("Lax");
        return serializer;
    }
}
