package com.fastcampus.book_bot.common.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    /** 네이버 API를 호출하기 위한 WebClient 설정
         - 기본 URL: https://openapi.naver.com/v1/search
         - 요청 헤더: Content-Type을 application/json으로 설정
         - 최대 메모리 크기: 4MB로 설정
         - 커넥션 타임아웃: 10초
         - 응답 타임아웃: 30초
         - 읽기/쓰기 타임아웃: 30초
    * */

    @Bean
    public WebClient naverAPIWebClient() {
        return WebClient.builder()
                .baseUrl("https://openapi.naver.com/v1/search")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(4 * 1024 * 1024);
                })
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                                .responseTimeout(Duration.ofSeconds(30))
                                .doOnConnected(conn ->
                                        conn.addHandlerLast(new ReadTimeoutHandler(30))
                                                .addHandlerLast(new WriteTimeoutHandler(30)))
                ))
                .build();
    }
}
