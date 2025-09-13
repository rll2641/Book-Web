package com.fastcampus.book_bot.service.order;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.dto.book.BookSalesDTO;
import com.fastcampus.book_bot.repository.BookRepository;
import com.fastcampus.book_bot.repository.OrderBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BestSellerService {

    private final OrderBookRepository orderBookRepository;
    private final BookRepository bookRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String WEEKLY_BESTSELLER_KEY = "bestseller:weekly";
    private static final String MONTHLY_BESTSELLER_KEY = "bestseller:monthly";

    @Transactional
    public List<Book> getWeekBestSeller() {

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        List<BookSalesDTO> weekSalesList = orderBookRepository.findWeeklyBestSellers(weekAgo);

        List<Book> bookList = new ArrayList<>();

        for (BookSalesDTO bookSalesDTO : weekSalesList) {
            Optional<Book> book = bookRepository.findById(bookSalesDTO.getBookId());
            book.ifPresent(bookList::add);
        }

        redisTemplate.opsForValue().set(WEEKLY_BESTSELLER_KEY, bookList, Duration.ofDays(7));

        return bookList;
    }

    @Transactional
    public List<Book> getMonthBestSeller() {

        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

        List<BookSalesDTO> monthlySalesList = orderBookRepository.findMonthlyBestSellers(monthAgo);

        List<Book> bookList = new ArrayList<>();

        for (BookSalesDTO bookSalesDTO : monthlySalesList) {
            Optional<Book> book = bookRepository.findById(bookSalesDTO.getBookId());
            book.ifPresent(bookList::add);
        }

        redisTemplate.opsForValue().set(MONTHLY_BESTSELLER_KEY, bookList, Duration.ofDays(30));

        return bookList;
    }
}
