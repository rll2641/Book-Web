package com.fastcampus.book_bot.domain.user.strategy;

import java.math.BigDecimal;

public interface GradeStrategy {
    BigDecimal calculateDiscount(BigDecimal orderAmount);
    Integer calculateMileage(BigDecimal orderAmount);
    boolean canFreeShipping();
    String getGradeName();
}
