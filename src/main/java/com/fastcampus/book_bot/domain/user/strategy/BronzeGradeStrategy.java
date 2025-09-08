package com.fastcampus.book_bot.domain.user.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BronzeGradeStrategy implements GradeStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        return orderAmount.multiply(BigDecimal.valueOf(0));
    }

    @Override
    public Integer calculateMileage(BigDecimal orderAmount) {
        return orderAmount.multiply(BigDecimal.valueOf(0)).intValue();
    }

    @Override
    public boolean canFreeShipping() {
        return false;
    }

    @Override
    public String getGradeName() {
        return "Bronze";
    }
}
