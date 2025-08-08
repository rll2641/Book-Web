package com.fastcampus.book_bot.controller.api;

import com.fastcampus.book_bot.common.response.ApiResponse;
import com.fastcampus.book_bot.dto.api.NaverBookResponseDTO;
import com.fastcampus.book_bot.service.api.ApiToMySQLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Slf4j
public class NaverBookApiController {

    private final ApiToMySQLService apiToMySQLService;

    public NaverBookApiController(ApiToMySQLService apiToMySQLService) {
        this.apiToMySQLService = apiToMySQLService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<NaverBookResponseDTO>> searchAndSaveBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "10") int display) {

        log.info("도서 검색 요청: query={}, start={}, display={}", query, start, display);

        ApiResponse<NaverBookResponseDTO> apiResponse = apiToMySQLService.searchAndSaveBooks(query, start, display);

        if (apiResponse.getSuccess()) {
            return ResponseEntity.ok(apiResponse);
        } else {
            return ResponseEntity.badRequest().body(apiResponse);
        }

    }
}
