package com.fastcampus.book_bot.domain.user.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SilverGradeStrategy implements GradeStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        return orderAmount.multiply(BigDecimal.valueOf(0.03));
    }

    @Override
    public Integer calculateMileage(BigDecimal orderAmount) {
        return orderAmount.multiply(BigDecimal.valueOf(0.03)).intValue();
    }

    @Override
    public boolean canFreeShipping() {
        return false;
    }

    @Override
    public String getGradeName() {
        return "SILVER";
    }
}
