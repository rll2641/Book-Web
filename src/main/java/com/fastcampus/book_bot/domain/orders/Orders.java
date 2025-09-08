package com.fastcampus.book_bot.domain.orders;

import com.fastcampus.book_bot.domain.payment.Payment;
import com.fastcampus.book_bot.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Orders {

    /**
     * 주문 ID (Primary Key)
     * 시스템 내부에서 주문을 고유하게 식별하는 자동 증가 값
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ID")
    private Integer orderId;

    /**
     * 주문 상태
     * 주문의 처리 단계를 나타냄
     * 결제와는 별개의 물류/배송 상태
     * 'ORDER_READY', 'ORDER_PROCESSING', 'SHIPPED', 'DELIVERED'
     */
    @Column(name = "ORDER_STATUS", nullable = false, length = 30)
    private String orderStatus = "ORDER_READY";

    /**
     * 총 주문 금액
     * order_book 테이블의 (PRICE * QUANTITY) 합계
     * 계산된 값이므로 비정규화된 필드
     */
    @Column(name = "TOTAL_PRICE")
    private Integer totalPrice;

    /**
     * 주문 날짜 (DATE 타입)
     * 주문이 생성된 날짜만 저장 (시간 정보 없음)
     * 일별 통계나 리포트에서 사용
     */
    @Column(name = "ORDER_DAY")
    private LocalDate orderDay;

    /**
     * 주문 일시 (TIMESTAMP 타입)
     * 주문이 생성된 정확한 시간
     * 상세한 시간 정보가 필요한 경우 사용
     */
    @Column(name = "ORDER_DATE")
    private LocalDateTime orderDate;

    /**
     * 생성 시간
     * 레코드가 생성된 시간 (자동 설정)
     */
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     * 레코드가 마지막으로 수정된 시간 (자동 업데이트)
     */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /**
     * 주문한 사용자 정보
     * N:1 관계 - 여러 주문이 한 사용자에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    /**
     * 주문 상품 목록
     * 1:N 관계 - 한 주문에 여러 도서가 포함됨
     * CascadeType.ALL: 주문 삭제 시 주문상품도 함께 삭제
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderBook> orderBooks = new ArrayList<>();

    /**
     * 결제 정보
     * 1:1 관계 - 한 주문에 하나의 결제
     */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;
}
