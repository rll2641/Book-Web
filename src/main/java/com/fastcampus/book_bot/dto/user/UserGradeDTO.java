package com.fastcampus.book_bot.dto.user;

import com.fastcampus.book_bot.domain.user.UserGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGradeDTO {

    private Integer gradeId;
    private String gradeName;
    private BigDecimal discount;
    private BigDecimal mileageRate;
    private Integer minUsage;
    private Integer orderCount;

    public UserGradeDTO(UserGrade userGrade) {
        if (userGrade != null) {
            this.gradeId = userGrade.getGradeId();
            this.gradeName = userGrade.getGradeName();
            this.discount = userGrade.getDiscount();
            this.mileageRate = userGrade.getMileageRate();
            this.minUsage = userGrade.getMinUsage();
            this.orderCount = userGrade.getOrderCount();
        }
    }
}