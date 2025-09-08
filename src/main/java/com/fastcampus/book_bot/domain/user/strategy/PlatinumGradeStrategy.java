package com.fastcampus.book_bot.domain.user.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PlatinumGradeStrategy implements GradeStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        return orderAmount.multiply(BigDecimal.valueOf(0.08));
    }

    @Override
    public Integer calculateMileage(BigDecimal orderAmount) {
        return orderAmount.multiply(BigDecimal.valueOf(0.08)).intValue();
    }

    @Override
    public boolean canFreeShipping() {
        return true;
    }

    @Override
    public String getGradeName() {
        return "PLATINUM";
    }
}
