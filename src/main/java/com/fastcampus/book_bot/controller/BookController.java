package com.fastcampus.book_bot.controller;

import com.fastcampus.book_bot.domain.Book;
import com.fastcampus.book_bot.service.api.ApiToMySQLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Slf4j
public class BookController {

    private final ApiToMySQLService apiToMySQLService;

    public BookController(ApiToMySQLService apiToMySQLService) {
        this.apiToMySQLService = apiToMySQLService;
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchAndSaveBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "10") int display) {

        log.info("도서 검색 요청: query={}, start={}, display={}", query, start, display);

        List<Book> bookList = apiToMySQLService.searchAndSaveBooks(query, start, display);

        return ResponseEntity.ok("도서 저장 완료: " + bookList.size());
    }
}
