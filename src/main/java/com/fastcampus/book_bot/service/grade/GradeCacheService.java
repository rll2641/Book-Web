package com.fastcampus.book_bot.service.grade;

import com.fastcampus.book_bot.domain.user.UserGrade;
import com.fastcampus.book_bot.dto.grade.GradeInfo;
import com.fastcampus.book_bot.repository.UserGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GradeCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserGradeRepository userGradeRepository;

    private static final String GRADE_CACHE_KEY = "grade:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Transactional
    public GradeInfo getGradeInfo(String gradeName) {

        String cacheKey = GRADE_CACHE_KEY + gradeName;

        // Redis 조회
        GradeInfo cached = (GradeInfo) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 캐시에 없으면 DB에서 조회 후 캐시 저장
        return userGradeRepository.findByGradeName(gradeName)
                .map(this::convertToGradeInfo)
                .map(gradeInfo -> {
                    redisTemplate.opsForValue().set(cacheKey, gradeInfo, CACHE_TTL);
                    return gradeInfo;
                })
                .orElse(getDefaultGradeInfo());

    }

    public void evictGradeCache(String gradeName) {
        redisTemplate.delete(GRADE_CACHE_KEY + gradeName);
    }

    public void evictAllGradeCache() {
        Set<String> keys = redisTemplate.keys(GRADE_CACHE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private GradeInfo convertToGradeInfo(UserGrade userGrade) {
        return GradeInfo.builder()
                .gradeName(userGrade.getGradeName())
                .minUsage(userGrade.getMinUsage())
                .orderCount(userGrade.getOrderCount())
                .discount(userGrade.getDiscount())
                .mileageRate(userGrade.getMileageRate())
                .build();
    }

    private GradeInfo getDefaultGradeInfo() {
        return GradeInfo.builder()
                .gradeName("BRONZE")
                .minUsage(100)
                .orderCount(1)
                .discount(BigDecimal.valueOf(0.0))
                .mileageRate(BigDecimal.valueOf(0.0))
                .build();
    }
}
