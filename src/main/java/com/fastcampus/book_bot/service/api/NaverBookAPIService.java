package com.fastcampus.book_bot.service.api;

import com.fastcampus.book_bot.dto.api.NaverBookResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class NaverBookAPIService {

    /* 네이버 API 요청&응답 클래스 작성
        - 비동기식
        - 응답 타임아웃 30초
    * */

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    private final WebClient webClient;

    public NaverBookAPIService(WebClient webClient) {
        this.webClient = webClient;
    }

    public NaverBookResponseDTO searchBooks(String query, int start, int display) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/book.json")
                        .queryParam("query", query)
                        .queryParam("start", start)
                        .queryParam("display", display)
                        .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> {
                            log.warn("클라이언트 오류: {}", response.statusCode());
                            return Mono.error(new RuntimeException("Client Error: " + response.statusCode()));
                        })
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> {
                            log.warn("서버 오류: {}", response.statusCode());
                            return Mono.error(new RuntimeException("Server Error: " + response.statusCode()));
                        })
                .bodyToMono(NaverBookResponseDTO.class)
                .timeout(Duration.ofSeconds(30))
                .doOnNext(response -> log.debug("API 응답 성공: 총 {} 건", response.getTotal()))
                .doOnError(error -> log.error("네이버 도서 API 비동기 호출 실패: {}", error.getMessage()))
                .onErrorReturn(new NaverBookResponseDTO())
                .block(Duration.ofSeconds(30));
    }
}
