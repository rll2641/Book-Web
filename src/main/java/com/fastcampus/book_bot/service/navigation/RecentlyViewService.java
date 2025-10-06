package com.fastcampus.book_bot.service.navigation;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.dto.api.BookDTO;
import com.fastcampus.book_bot.service.book.BookCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentlyViewService {

    private final StringRedisTemplate redisTemplate;
    private final BookCacheService bookCacheService;

    private static final String RECENTLY_KEY = "recent:view:";
    private static final String BOOK_CACHE_KEY = "book:";
    private static final int MAX_RECENT_ITEMS = 5;

    /**
     * 최근 본 상품 추가
     * - 이미 존재 시, timestamp만 업데이트
     * - MAX_RECENT_ITEMS 초과 시 오래된 항목 삭제
     */
    public void addRecentBook(Integer userId, Integer bookId) {
        String key = RECENTLY_KEY + userId;
        double score = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(key, bookId.toString(), score);

        String bookCacheKey = BOOK_CACHE_KEY + bookId;
        Map<Object, Object> existingData = redisTemplate.opsForHash().entries(bookCacheKey);

        if (existingData == null || existingData.isEmpty()) {
            log.info("북 캐시 없음 - 새로 저장: {}", bookCacheKey);
            Book book = bookCacheService.getBook(bookId);
            bookCacheService.setBookRedis(bookId, book);
        } else {
            log.debug("북 캐시 존재 - bookId: {}", bookId);
        }

        Long size = redisTemplate.opsForZSet().size(key);
        if (size != null && size > MAX_RECENT_ITEMS) {
            redisTemplate.opsForZSet().removeRange(key, 0, size - MAX_RECENT_ITEMS - 1);
        }

        redisTemplate.expire(key, Duration.ofDays(30));
    }

    /**
     * 최근 본 상품 조회
     */
    public List<Book> getRecentBookIds(Integer userId) {
        String key = RECENTLY_KEY + userId;
        log.info("최근 본 상품 조회 시작 - UserId: {}, Key: {}", userId, key);

        Set<String> bookIds = redisTemplate.opsForZSet()
                .reverseRange(key, 0, MAX_RECENT_ITEMS - 1);

        log.info("Redis ZSet 조회 결과 - bookIds: {}", bookIds);

        if (bookIds == null || bookIds.isEmpty()) {
            log.warn("Redis에서 최근 본 상품이 없음 - UserId: {}", userId);
            return Collections.emptyList();
        }

        List<Book> books = new ArrayList<>();
        for (String bookId : bookIds) {
            String bookCacheKey = BOOK_CACHE_KEY + bookId;
            log.debug("북 캐시 조회 - Key: {}", bookCacheKey);

            Map<Object, Object> bookData = redisTemplate.opsForHash().entries(bookCacheKey);
            log.debug("북 데이터 조회 결과 - bookId: {}, dataSize: {}", bookId, bookData.size());

            if (!bookData.isEmpty()) {
                Book book = convertHashToBook(bookData);
                books.add(book);
                log.debug("북 추가 완료 - bookId: {}, bookName: {}", book.getBookId(), book.getBookName());
            } else {
                log.warn("북 캐시 데이터 없음 - bookCacheKey: {}", bookCacheKey);
            }
        }

        log.info("최근 본 상품 조회 완료 - UserId: {}, 조회된 책 수: {}", userId, books.size());
        return books;
    }

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
}
