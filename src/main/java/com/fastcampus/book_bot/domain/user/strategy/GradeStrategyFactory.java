package com.fastcampus.book_bot.domain.user.strategy;

import com.fastcampus.book_bot.domain.user.UserGrade;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GradeStrategyFactory {

    private final Map<String, GradeStrategy> strategies;

    public GradeStrategyFactory(List<GradeStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        GradeStrategy::getGradeName,
                        strategy -> strategy
                ));
    }

    public GradeStrategy getStrategy(String gradeName) {
        GradeStrategy strategy = strategies.get(gradeName.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 등급입니다." + gradeName);
        }
        return strategy;
    }

    public GradeStrategy getStrategy(UserGrade userGrade) {
        return getStrategy(userGrade.getGradeName());
    }
}
