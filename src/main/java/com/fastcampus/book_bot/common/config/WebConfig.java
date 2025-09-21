package com.fastcampus.book_bot.common.config;

//import com.fastcampus.book_bot.common.intercept.AuthInterceptor;
import com.fastcampus.book_bot.common.intercept.SessionAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

//    private final AuthInterceptor authInterceptor;
    private final SessionAuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(authInterceptor)
//                .addPathPatterns("/order/**", "/api/order/**")
//                .excludePathPatterns("/login", "/register");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/member/login",
                        "/api/member/signup",
                        "/api/member/check-**",
                        "/api/member/send-**",
                        "/api/member/verify-**"
                );
    }
}
