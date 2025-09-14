package com.fastcampus.book_bot.service.grade;

import com.fastcampus.book_bot.domain.user.UserGrade;
import com.fastcampus.book_bot.dto.grade.GradeInfo;
import com.fastcampus.book_bot.repository.UserGradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserGradeRepository userGradeRepository;

    private static final String GRADE_CACHE_KEY = "grade:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Transactional(readOnly = true)
    public GradeInfo getGradeInfo(String gradeName) {
        String cacheKey = GRADE_CACHE_KEY + gradeName;

        try {
            // Redis 조회
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);

            if (cachedValue != null) {
                // LinkedHashMap인 경우 GradeInfo 객체로 변환
                if (cachedValue instanceof LinkedHashMap) {
                    LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) cachedValue;
                    return mapToGradeInfo(map);
                }
                // 이미 GradeInfo 객체인 경우
                else if (cachedValue instanceof GradeInfo) {
                    return (GradeInfo) cachedValue;
                }
            }
        } catch (Exception e) {
            log.warn("Redis에서 등급 정보 조회 실패: {}, DB에서 조회합니다.", gradeName, e);
        }

        // 캐시에 없거나 오류 발생 시 DB에서 조회
        return userGradeRepository.findByGradeName(gradeName)
                .map(this::convertToGradeInfo)
                .map(gradeInfo -> {
                    try {
                        redisTemplate.opsForValue().set(cacheKey, gradeInfo, CACHE_TTL);
                    } catch (Exception e) {
                        log.warn("Redis 캐시 저장 실패: {}", gradeName, e);
                    }
                    return gradeInfo;
                })
                .orElse(getDefaultGradeInfo());
    }

    public void evictGradeCache(String gradeName) {
        try {
            redisTemplate.delete(GRADE_CACHE_KEY + gradeName);
        } catch (Exception e) {
            log.warn("Redis 캐시 삭제 실패: {}", gradeName, e);
        }
    }

    public void evictAllGradeCache() {
        try {
            Set<String> keys = redisTemplate.keys(GRADE_CACHE_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis 전체 캐시 삭제 실패", e);
        }
    }

    // LinkedHashMap을 GradeInfo로 변환하는 헬퍼 메서드
    private GradeInfo mapToGradeInfo(LinkedHashMap<String, Object> map) {
        try {
            return GradeInfo.builder()
                    .gradeName((String) map.get("gradeName"))
                    .minUsage(map.get("minUsage") != null ? ((Number) map.get("minUsage")).intValue() : 0)
                    .orderCount(map.get("orderCount") != null ? ((Number) map.get("orderCount")).intValue() : 0)
                    .discount(map.get("discount") != null ?
                            new BigDecimal(map.get("discount").toString()) : BigDecimal.ZERO)
                    .mileageRate(map.get("mileageRate") != null ?
                            new BigDecimal(map.get("mileageRate").toString()) : BigDecimal.ZERO)
                    .build();
        } catch (Exception e) {
            log.error("LinkedHashMap을 GradeInfo로 변환 실패: {}", map, e);
            return getDefaultGradeInfo();
        }
    }

    private GradeInfo convertToGradeInfo(UserGrade userGrade) {
        return GradeInfo.builder()
                .gradeName(userGrade.getGradeName())
                .minUsage(userGrade.getMinUsage())
                .orderCount(userGrade.getOrderCount())
                .discount(userGrade.getDiscount() != null ?
                        userGrade.getDiscount() : BigDecimal.ZERO)
                .mileageRate(userGrade.getMileageRate() != null ?
                        userGrade.getMileageRate() : BigDecimal.ZERO)
                .build();
    }

    private GradeInfo getDefaultGradeInfo() {
        return GradeInfo.builder()
                .gradeName("BRONZE")
                .minUsage(100)
                .orderCount(1)
                .discount(BigDecimal.ZERO)
                .mileageRate(BigDecimal.ZERO)
                .build();
    }
}