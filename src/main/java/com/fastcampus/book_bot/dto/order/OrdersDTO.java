package com.fastcampus.book_bot.dto.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrdersDTO {

    private Integer bookId;
    private String bookName;
    private String bookAuthor;
    private String bookPublisher;
    private Integer price;
    private Integer quantity;
}
