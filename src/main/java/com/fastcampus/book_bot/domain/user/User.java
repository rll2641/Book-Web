package com.fastcampus.book_bot.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID", nullable = false, updatable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GRADE_ID", nullable = false, insertable = false)
    private UserGrade userGrade;

    @Column(name = "USER_EMAIL", length = 30)
    private String userEmail;

    @Column(name = "USER_PASSWORD", length = 30)
    private String userPassword;

    @Column(name = "USER_NAME", length = 30)
    private String userName;

    @Column(name = "USER_NICKNAME", length = 20)
    private String userNickname;

    @Column(name = "USER_PHONE", length = 30)
    private String userPhone;

    @Column(name = "USER_STATUS", length = 50, nullable = false)
    private String userStatus;

    @Column(name = "POINT", insertable = false)
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