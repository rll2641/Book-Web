package com.fastcampus.book_bot.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID", nullable = false, updatable = false)
    private Long UserId;

    @Column(name = "USER_EMAIL", nullable = false)
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String UserEmail;

    @Column(name = "USER_PASSWORD", length = 12, nullable = false)
    @Size(min = 8, max = 12, message = "비밀번호는 8~12자여야 합니다.")
    private String UserPassword;

    @Column(name = "USER_NAME", length = 50, nullable = false)
    private String userName;

    @Column(name = "USER_PHONE", length = 20)
    private String userPhone;

    @Column(name = "USER_STATUS")
    @Check(constraints = "USER_STATUS IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')")
    private String userStatus = "ACTIVE";

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
