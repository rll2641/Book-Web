package com.fastcampus.book_bot.common.cache;

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
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 주간/월간 베스트셀러 존재하지 않을 시, 수동 실행 (분리 필요)
     * */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("캐시 워밍 시작");

        boolean weeklyExists = redisTemplate.hasKey("bestseller:weekly");
        boolean monthlyExists = redisTemplate.hasKey("bestseller:monthly");

        if(!weeklyExists) {
            log.info("주간 베스트셀러 캐시 없음");
            bestSellerService.updateWeeklyBestSeller();
        } else {
            log.info("주간 베스트셀러 캐시 존재");
        }

        if(!monthlyExists) {
            log.info("월간 베스트셀러 캐시 없음");
            bestSellerService.updateMonthBestSeller();
        } else {
            log.info("월간 베스트셀러 캐시 존재");
        }
    }
}
