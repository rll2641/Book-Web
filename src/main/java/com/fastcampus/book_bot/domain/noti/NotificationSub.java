package com.fastcampus.book_bot.domain.noti;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "notification_sub")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "threshold_quantity", nullable = false)
    @Builder.Default
    private Integer thresholdQuantity = 5;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;

    @OneToMany(mappedBy = "notificationSub", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationLogs> notificationLogs;
}
