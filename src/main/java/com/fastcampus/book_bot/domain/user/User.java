package com.fastcampus.book_bot.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID", nullable = false, updatable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GRADE_ID", nullable = false)
    private UserGrade userGrade;

    @Column(name = "USER_EMAIL", length = 30)
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String userEmail;

    @Column(name = "USER_PASSWORD", length = 30)
    private String userPassword;

    @Column(name = "USER_NAME", length = 30)
    private String userName;

    @Column(name = "USER_PHONE", length = 30)
    private String userPhone;

    @Column(name = "USER_STATUS", length = 50, nullable = false)
    private String userStatus = "ACTIVE";

    @Column(name = "POINT")
    private Integer point;

    @Column(name = "POSTCODE")
    private Integer postcode;

    @Column(name = "DEFAULT_ADDRESS", length = 200)
    private String defaultAddress;

    @Column(name = "DETAIL_ADDRESS", length = 200)
    private String detailAddress;

    @Column(name = "CITY", length = 50)
    private String city;

    @Column(name = "PROVINCE", length = 50)
    private String province;

    @Column(name = "LAST_LOGIN")
    private LocalDateTime lastLogin;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}