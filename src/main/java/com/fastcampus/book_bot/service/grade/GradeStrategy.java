package com.fastcampus.book_bot.service.grade;

public interface GradeStrategy {

    int calculateDiscountedPrice(int originalAmount);
    int calculateShippingCost(int orderAmount);
    int calculateMileage(int orderAmount);
    String getGradeName();
}
