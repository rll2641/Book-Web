package com.fastcampus.book_bot.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookSalesDTO {
    private Integer bookId;
    private Long totalQuantity;

}
