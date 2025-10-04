package com.fastcampus.book_bot.service.book;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final BookRepository bookRepository;

    private static final String BOOK_CACHE = "book:";
    private static final String BOOK_QUANTITY = "book:quantity:";

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
     * 도서를 Redis에 저장 (Book 객체 + 재고 수량)
     */
    public void setBookRedis(Integer bookId, Book book) {
        String bookCacheKey = BOOK_CACHE + bookId;
        String quantityKey = BOOK_QUANTITY + bookId;

        redisTemplate.opsForValue().set(bookCacheKey, book, Duration.ofDays(7));

        stringRedisTemplate.opsForValue().set(
                quantityKey,
                String.valueOf(book.getBookQuantity()),
                Duration.ofDays(7)
        );

        log.info("Redis 저장 완료 - BookId: {}, 재고: {}", bookId, book.getBookQuantity());
    }

    /**
     * Redis에서 재고 조회
     */
    public Integer getBookQuantity(Integer bookId) {
        String quantityKey = BOOK_QUANTITY + bookId;

        String quantity = stringRedisTemplate.opsForValue().get(quantityKey);

        if (quantity != null) {
            return Integer.valueOf(quantity);
        }

        return null;
    }

    /**
     * Lua Script를 사용한 원자적 재고 차감
     */
    public Long decrementBookQuantity(Integer bookId, Integer quantity) {
        String quantityKey = BOOK_QUANTITY + bookId;

        String luaScript =
                "local current = redis.call('get', KEYS[1]) " +
                        "if current == false then " +
                        "    return -1 " +
                        "end " +
                        "current = tonumber(current) " +
                        "if current == nil then " +
                        "    return -3 " +
                        "end " +
                        "if current < tonumber(ARGV[1]) then " +
                        "    return -2 " +
                        "end " +
                        "return redis.call('decrby', KEYS[1], ARGV[1])";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);

        Long result = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(quantityKey),
                quantity.toString()
        );

        if (result == null) {
            log.error("Lua Script 실행 실패 - BookId: {}", bookId);
            throw new RuntimeException("재고 차감 실패");
        }

        if (result == -1) {
            log.error("재고 정보 없음 - BookId: {}", bookId);
            throw new IllegalArgumentException("재고 정보가 존재하지 않습니다: " + bookId);
        }

        if (result == -3) {
            log.error("재고 데이터 타입 오류 - BookId: {}", bookId);
            throw new IllegalStateException("재고 데이터 타입 오류. Redis 데이터를 확인하세요.");
        }

        if (result == -2) {
            Integer currentStock = getBookQuantity(bookId);
            log.error("재고 부족 - BookId: {}, 요청수량: {}, 현재재고: {}",
                    bookId, quantity, currentStock);
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + currentStock);
        }

        log.info("원자적 재고 차감 성공 - BookId: {}, 차감수량: {}, 남은재고: {}",
                bookId, quantity, result);

        updateBookObjectQuantity(bookId, result.intValue());

        return result;
    }

    /**
     * Book 객체의 재고 수량 업데이트
     */
    private void updateBookObjectQuantity(Integer bookId, Integer newQuantity) {
        String bookCacheKey = BOOK_CACHE + bookId;
        Book cachedBook = (Book) redisTemplate.opsForValue().get(bookCacheKey);

        if (cachedBook != null) {
            cachedBook.setBookQuantity(newQuantity);
            redisTemplate.opsForValue().set(bookCacheKey, cachedBook, Duration.ofDays(7));
            log.debug("Book 객체 재고 업데이트 완료 - BookId: {}, 새 재고: {}", bookId, newQuantity);
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
            int top20Count = (int) Math.ceil(totalCount * 0.2);

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