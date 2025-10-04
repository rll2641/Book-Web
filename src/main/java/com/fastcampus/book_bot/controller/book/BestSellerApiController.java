package com.fastcampus.book_bot.controller.book;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.service.order.BestSellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BestSellerApiController {

    private final BestSellerService bestSellerService;

    @GetMapping("/api/bestseller/redis")
    public List<Book> getBestSellerRedis() {
        return bestSellerService.getMonthBestSeller();
    }

    @GetMapping("/api/bestseller/db")
    public List<Book> getBestSellerDB() {
        return bestSellerService.getMonthBestSellerFromDB();
    }
}
