package com.fastcampus.book_bot.service.order;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.dto.book.BookSalesDTO;
import com.fastcampus.book_bot.repository.BookRepository;
import com.fastcampus.book_bot.repository.OrderBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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

        try {
            Object cached = redisTemplate.opsForValue().get(WEEKLY_BESTSELLER_KEY);

            if (cached instanceof List<?>) {
                log.info("주간 베스트셀러 캐시 조회 성공");
                return (List<Book>) cached;
            }
            log.warn("주간 베스트셀러 캐시가 없습니다.");
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("주간 베스트셀러 캐시 조회 실패",e);
            return new ArrayList<>();
        }
    }

    @Transactional
    public List<Book> getMonthBestSeller() {

        try {
            Object cached = redisTemplate.opsForValue().get(MONTHLY_BESTSELLER_KEY);
            if (cached instanceof List<?>) {
                log.info("월간 베스트셀러 캐시 조회 성공");
                return (List<Book>) cached;
            }
            log.warn("월간 베스트셀러 캐시가 없습니다.");
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("월간 베스트셀러 캐시 조회 실패", e);
            return new ArrayList<>();
        }
    }

    @Scheduled(cron = "0 0 4 * * MON")
    @Transactional(readOnly = true)
    public void updateWeeklyBestSeller() {

        try {
            log.info("주간 베스트셀러 캐시 갱신 시작");

            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<BookSalesDTO> weeklyBestSellers = orderBookRepository.findWeeklyBestSellers(weekAgo);

            List<Book> bookList = new ArrayList<>();
            for (BookSalesDTO bookSalesDTO : weeklyBestSellers) {
                Optional<Book> book = bookRepository.findById(bookSalesDTO.getBookId());
                book.ifPresent(bookList::add);
            }

            redisTemplate.opsForValue().set(WEEKLY_BESTSELLER_KEY, bookList, Duration.ofDays(7));
            log.info("주간 베스트셀러 캐시 갱신 완료");
        } catch (Exception e) {
            log.error("주간 베스트셀러 캐시 갱신 실패", e);
        }
    }

    @Scheduled(cron = "0 0 4 1 * *")
    @Transactional(readOnly = true)
    public void updateMonthBestSeller() {

        try {
            log.info("월간 베스트셀러 캐시 갱신 시작");

            LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
            List<BookSalesDTO> monthlySalesList = orderBookRepository.findMonthlyBestSellers(monthAgo);

            List<Book> bookList = new ArrayList<>();
            for (BookSalesDTO bookSalesDTO : monthlySalesList) {
                Optional<Book> book = bookRepository.findById(bookSalesDTO.getBookId());
                book.ifPresent(bookList::add);
            }

            redisTemplate.opsForValue().set(MONTHLY_BESTSELLER_KEY, bookList, Duration.ofDays(30));
            log.info("월간 베스트셀러 캐시 갱신 완료: {} 건", bookList.size());
        } catch (Exception e) {
            log.error("월간 베스트셀러 캐시 갱신 실패", e);
        }
    }
}
