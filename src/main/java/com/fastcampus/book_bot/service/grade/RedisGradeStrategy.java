package com.fastcampus.book_bot.service.grade;

import com.fastcampus.book_bot.dto.grade.GradeInfo;

import java.math.BigDecimal;

public class RedisGradeStrategy implements GradeStrategy {

    private static final int SHIPPING_COST = 3000;
    private final GradeInfo gradeInfo;

    public RedisGradeStrategy(GradeInfo gradeInfo) {
        this.gradeInfo = gradeInfo;
    }

    @Override
    public int calculateDiscountedPrice(int originalAmount) {
        return BigDecimal.valueOf(originalAmount)
                .multiply(BigDecimal.ONE.subtract(gradeInfo.getDiscount()))
                .intValue();
    }

    @Override
    public int calculateShippingCost(int orderAmount) {
        return orderAmount >= gradeInfo.getMinUsage() ? 0 : SHIPPING_COST;
    }

    public int calculateMileage(int orderAmount) {
        return BigDecimal.valueOf(orderAmount)
                .multiply(gradeInfo.getMileageRate())
                .intValue();
    }

    @Override
    public String getGradeName() {
        return gradeInfo.getGradeName();
    }
}
