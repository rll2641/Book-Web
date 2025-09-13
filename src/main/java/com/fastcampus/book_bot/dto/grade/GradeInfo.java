package com.fastcampus.book_bot.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeInfo implements Serializable {
    private String gradeName;
    private int minUsage;
    private int orderCount;
    private BigDecimal discount;
    private BigDecimal mileageRate;
}
