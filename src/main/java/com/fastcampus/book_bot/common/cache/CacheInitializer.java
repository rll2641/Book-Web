package com.fastcampus.book_bot.common.cache;

import com.fastcampus.book_bot.service.book.BookCacheService;
import com.fastcampus.book_bot.service.order.BestSellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInitializer implements ApplicationRunner {

    private final BestSellerService bestSellerService;
    private final BookCacheService bookCacheService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 애플리케이션 시작 시 캐시 초기화
     * - 주간/월간 베스트셀러 캐시 확인 및 생성
     * - 주문량 상위 20% 도서 Redis 캐싱
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== 캐시 워밍 시작 ===");


        initTopBooksCache();

        initBestSellerCache();

        log.info("=== 캐시 워밍 완료 ===");
    }

    /**
     * 베스트셀러 캐시 초기화
     */
    private void initBestSellerCache() {
        boolean weeklyExists = redisTemplate.hasKey("bestseller:weekly");
        boolean monthlyExists = redisTemplate.hasKey("bestseller:monthly");

        if (!weeklyExists) {
            log.info("주간 베스트셀러 캐시 없음 - 생성 시작");
            bestSellerService.updateWeeklyBestSeller();
        } else {
            log.info("주간 베스트셀러 캐시 존재");
        }

        if (!monthlyExists) {
            log.info("월간 베스트셀러 캐시 없음 - 생성 시작");
            bestSellerService.updateMonthBestSeller();
        } else {
            log.info("월간 베스트셀러 캐시 존재");
        }
    }

    /**
     * 주문량 상위 20% 도서 캐시 초기화
     */
    private void initTopBooksCache() {
        try {
            log.info("주문량 상위 20% 도서 Redis 캐싱 시작");
            bookCacheService.BookToRedis();
            log.info("주문량 상위 20% 도서 Redis 캐싱 완료");
        } catch (Exception e) {
            log.error("주문량 상위 20% 도서 Redis 캐싱 실패", e);
        }
    }
}