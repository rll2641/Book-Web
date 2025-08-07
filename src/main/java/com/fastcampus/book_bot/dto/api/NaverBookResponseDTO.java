package com.fastcampus.book_bot.dto.api;

import lombok.*;

import java.time.LocalDateTime;

@Data
public class NaverBookResponseDTO {

    private int total;
    private int start;
    private int display;
    private BookDTO[] items;
}
