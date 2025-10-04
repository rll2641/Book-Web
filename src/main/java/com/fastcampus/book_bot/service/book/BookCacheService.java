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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final BookRepository bookRepository;

    private static final String BOOK_CACHE = "book:";

    /**
     * Redis에서 도서 조회
     */
    public Book getBook(Integer bookId) {
        String cacheKey = BOOK_CACHE + bookId;

        Map<Object, Object> bookData = stringRedisTemplate.opsForHash().entries(cacheKey);

        if (!bookData.isEmpty()) {
            log.info("Redis Hash에서 도서 조회 성공 - BookId: {}", bookId);
            return convertHashToBook(bookData);
        }


        log.info("Redis 조회 실패 -> DB 조회 - BookId: {}", bookId);
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
    }

    /**
     * 도서를 Redis에 저장
     */
    public void setBookRedis(Integer bookId, Book book) {
        String cacheKey = BOOK_CACHE + bookId;

        Map<String, String> bookData = new HashMap<>();
        bookData.put("bookId", String.valueOf(book.getBookId()));
        bookData.put("title", book.getBookName());
        bookData.put("author", book.getBookAuthor() != null ? book.getBookAuthor() : "");
        bookData.put("publisher", book.getBookPublisher() != null ? book.getBookPublisher() : "");
        bookData.put("price", String.valueOf(book.getBookDiscount()));
        bookData.put("quantity", String.valueOf(book.getBookQuantity()));
        bookData.put("isbn", book.getBookIsbn() != null ? book.getBookIsbn() : "");

        stringRedisTemplate.opsForHash().putAll(cacheKey, bookData);
        stringRedisTemplate.expire(cacheKey, Duration.ofDays(7));

        log.info("Redis 저장 완료 - BookId: {}, 재고: {}", bookId, book.getBookQuantity());
    }

    /**
     * Redis에서 재고 조회
     */
    public Integer getBookQuantity(Integer bookId) {
        String cacheKey = BOOK_CACHE + bookId;

        Object quantity = stringRedisTemplate.opsForHash().get(cacheKey, "quantity");

        if (quantity != null) {
            return Integer.valueOf((String) quantity);
        }

        return null;
    }

    /**
     * Lua Script를 사용한 원자적 재고 차감
     */
    public Long decrementBookQuantity(Integer bookId, Integer quantity) {
        String cacheKey = BOOK_CACHE + bookId;

        String luaScript =
                // 1. Hash에서 quantity 필드 조회
                "local current = redis.call('HGET', KEYS[1], 'quantity') " +

                        // 2. 키가 없는 경우
                        "if current == false then " +
                        "    return -1 " +
                        "end " +

                        // 3. 숫자로 변환
                        "current = tonumber(current) " +
                        "if current == nil then " +
                        "    return -3 " +
                        "end " +

                        // 4. 재고 부족 체크
                        "if current < tonumber(ARGV[1]) then " +
                        "    return -2 " +
                        "end " +

                        // 5. 재고 차감 (원자적 연산!)
                        "return redis.call('HINCRBY', KEYS[1], 'quantity', -ARGV[1])";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);

        Long result = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(cacheKey),
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
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + currentStock + " BookId: " + bookId);
        }

        log.info("원자적 재고 차감 성공 - BookId: {}, 차감수량: {}, 남은재고: {}",
                bookId, quantity, result);

        return result;
    }

    /**
     * Hash 데이터를 Book 객체로 변환
     */
    private Book convertHashToBook(Map<Object, Object> bookData) {
        Book book = new Book();
        book.setBookId(Integer.valueOf((String) bookData.get("bookId")));
        book.setBookName((String) bookData.get("title"));
        book.setBookAuthor((String) bookData.get("author"));
        book.setBookPublisher((String) bookData.get("publisher"));
        book.setBookDiscount(Integer.valueOf((String) bookData.get("price")));
        book.setBookQuantity(Integer.valueOf((String) bookData.get("quantity")));
        book.setBookIsbn((String) bookData.get("isbn"));

        return book;
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