package com.fastcampus.book_bot.dto.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookDTO {

    private String title;
    private String link;
    private String image;
    private String author;
    private Integer discount;
    private String publisher;
    private Long isbn;
    private String description;
    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate pubdate;

}
