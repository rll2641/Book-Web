package com.fastcampus.book_bot.dto.order;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderCalculationResult {
    private Integer originalAmount;        // 원가
    private Integer gradeDiscountAmount;   // 등급 할인 금액
    private Integer usedPoints;           // 사용한 포인트
    private Integer shippingCost;         // 배송비
    private Integer finalAmount;          // 최종 결제 금액
    private Integer earnedMileage;        // 적립될 마일리지
    private String gradeName;             // 등급명
    private Integer afterDiscountAmount;  // 등급 할인 후 금액
}
