package com.fastcampus.book_bot.domain.user;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "User_Grade")
public class UserGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GRADE_ID", updatable = false, nullable = false)
    private Long gradeId;

    @Column(name = "GRADE_NAME", nullable = false)
    @Check(constraints = "GRADE_NAME IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM')")
    private String gradeName = "BRONZE";

    @Column(name = "ORDER_COUNT")
    private Integer orderCount = 0;

    @Column(name = "MIN_USAGE", nullable = false)
    private Integer MinUsage = 0;

    @Column(name = "DISCOUNT", nullable = false)
    private Float discount = 0.03f;

    @Column(name = "MILEAGE_RATE")
    private Float mileageRate = 0.03f;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private Integer updatedBy;
}
