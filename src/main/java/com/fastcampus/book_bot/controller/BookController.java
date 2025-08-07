package com.fastcampus.book_bot.controller;

import com.fastcampus.book_bot.domain.Book;
import com.fastcampus.book_bot.service.api.ApiToMySQLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Slf4j
public class BookController {

    private final ApiToMySQLService apiToMySQLService;

    public BookController(ApiToMySQLService apiToMySQLService) {
        this.apiToMySQLService = apiToMySQLService;
    }

    @PostMapping("/search")
    public Mono<ResponseEntity<List<Book>>> searchAndSaveBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "10") int display) {

        log.info("도서 검색 요청: query={}, start={}, display={}", query, start, display);

        return apiToMySQLService.searchAndSaveBooks(query, start, display)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
}
