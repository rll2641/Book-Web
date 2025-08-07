package com.fastcampus.book_bot.dto.api;

import lombok.*;

import java.time.LocalDateTime;

@Data
public class NaverBookResponseDTO {

    private int total;
    private int start;
    private int display;
    private NaverBookItemDTO[] items;

    @Data
    public static class NaverBookItemDTO {

        private String title;
        private String link;
        private String image;
        private String author;
        private int discount;
        private String publisher;
        private int isbn;
        private String description;
        private LocalDateTime pubdate;
    }
}
