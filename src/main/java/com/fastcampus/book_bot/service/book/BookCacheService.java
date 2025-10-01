package com.fastcampus.book_bot.service.book;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.repository.BookRepository;
import com.fastcampus.book_bot.repository.OrderBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final BookRepository bookRepository;

    private static final String BOOK_CACHE = "book:";

    /**
     * Redis에서 도서 조회
     */
    public Book getBook(Integer bookId) {
        String cacheKey = BOOK_CACHE + bookId;
        Book cachedBook = (Book) redisTemplate.opsForValue().get(cacheKey);

        if (cachedBook != null) {
            log.info("Redis에 도서 존재 - BookId: {}", bookId);
            return cachedBook;
        }

        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
    }

    /**
     * 도서를 Redis에 저장
     */
    public void setBookRedis(Integer bookId, Book book) {
        String cacheKey = BOOK_CACHE + bookId;
        redisTemplate.opsForValue().set(cacheKey, book, Duration.ofDays(7));
    }

    /**
     * Redis에서 재고 조회
     */
    public Integer getBookQuantity(Integer bookId) {
        String cacheKey = BOOK_CACHE + bookId;
        Book cachedBook = (Book) redisTemplate.opsForValue().get(cacheKey);

        if (cachedBook != null) {
            return cachedBook.getBookQuantity();
        }

        return null;
    }

    /**
     * Redis에서 도서 재고 차감
     */
    public void decrementBookQuantity(Integer bookId, Integer quantity) {
        String cacheKey = BOOK_CACHE + bookId;
        Book cachedBook = (Book) redisTemplate.opsForValue().get(cacheKey);

        if (cachedBook != null) {
            cachedBook.setBookQuantity(cachedBook.getBookQuantity() - quantity);
            redisTemplate.opsForValue().set(cacheKey, cachedBook, Duration.ofDays(7));

            log.info("Redis 도서 재고 차감 완료 - BookId: {}", bookId);
        }
    }

    /**
     * 주문량 상위 20% 도서를 Redis에 저장
     */
    @Scheduled(cron = "0 0 4 * * 1")
    @Transactional(readOnly = true)
    public void BookToRedis() {

        int cachedCount = 0;

        try {

            long totalCount = bookRepository.count();
            int top20Count = (int) Math.ceil(totalCount* 0.2);

            List<Book> books = bookRepository.findByTop20ByOrderCount(top20Count);

            for (Book book : books) {
                setBookRedis(book.getBookId(), book);
                cachedCount++;
            }

            log.info("Redis 저장 완료. 완료된 책 개수: {}", cachedCount);
        } catch (Exception e) {
            log.error("Redis 저장 실패. 완료된 책 개수: {}", cachedCount, e);
        }
    }

}
