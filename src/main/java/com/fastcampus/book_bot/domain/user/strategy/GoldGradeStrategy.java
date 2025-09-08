package com.fastcampus.book_bot.domain.user.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GoldGradeStrategy implements GradeStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        return orderAmount.multiply(BigDecimal.valueOf(0.05));
    }

    @Override
    public Integer calculateMileage(BigDecimal orderAmount) {
        return orderAmount.multiply(BigDecimal.valueOf(0.05)).intValue();
    }

    @Override
    public boolean canFreeShipping() {
        return true;
    }

    @Override
    public String getGradeName() {
        return "GOLD";
    }
}
