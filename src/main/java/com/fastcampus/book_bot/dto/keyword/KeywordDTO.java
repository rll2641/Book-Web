package com.fastcampus.book_bot.dto.keyword;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordDTO {

    /**
     * 검색어
     */
    private String keyword;

    /**
     * 검색 횟수
     */
    private Integer count;


    /**
     * 검색 날짜
     */
    private LocalDate searchDate;

    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;

    public KeywordDTO(String keyword, Integer count) {
        this.keyword = keyword;
        this.count = count;
    }
}
