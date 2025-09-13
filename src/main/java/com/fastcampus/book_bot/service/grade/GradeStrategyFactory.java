package com.fastcampus.book_bot.service.grade;

import com.fastcampus.book_bot.dto.grade.GradeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradeStrategyFactory {

    private final GradeCacheService gradeCacheService;

    public GradeStrategy getStrategy(String gradeName) {
        GradeInfo gradeInfo = gradeCacheService.getGradeInfo(gradeName);
        return new RedisGradeStrategy(gradeInfo);
    }
}
