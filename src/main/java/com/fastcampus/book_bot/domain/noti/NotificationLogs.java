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

@Entity
@Table(name = "notification_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sub_id", nullable = false)
    private Integer subId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "threshold_quantity", nullable = false)
    private Integer thresholdQuantity;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreatedDate
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id", insertable = false, updatable = false)
    private NotificationSub notificationSub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;
}
