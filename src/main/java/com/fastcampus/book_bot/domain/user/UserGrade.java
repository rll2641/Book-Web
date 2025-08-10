package com.fastcampus.book_bot.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER_GRADE")  // 테이블명 수정
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GRADE_ID", updatable = false, nullable = false)
    private Integer gradeId;

    @Column(name = "GRADE_NAME", nullable = false)
    private String gradeName = "BRONZE";

    @Column(name = "MIN_USAGE")
    private Integer minUsage;

    @Column(name = "ORDER_COUNT")
    private Integer orderCount;

    @Column(name = "DISCOUNT")
    private Float discount;

    @Column(name = "MILEAGE_RATE")
    private Float mileageRate;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private Integer updatedBy;
}