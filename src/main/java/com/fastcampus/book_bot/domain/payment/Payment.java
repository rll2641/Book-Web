package com.fastcampus.book_bot.domain.payment;

import com.fastcampus.book_bot.domain.orders.Orders;
import com.fastcampus.book_bot.domain.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Payment {

    /**
     * 결제 ID (Primary Key)
     * 시스템 내부에서 결제를 고유하게 식별하는 자동 증가 값
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Integer paymentId;

    /**
     * PG사 구분자 (전략패턴의 핵심)
     * 어떤 결제 전략(PG사)을 사용했는지 구분
     * 런타임에 전략 선택을 위한 중요한 필드
     */
    @Column(name = "PG_PROVIDER", nullable = false, length = 30)
    private String pgProvider;

    /**
     * 결제 고유키
     * 각 PG사에서 발급하는 결제 식별자
     * - 토스페이먼츠: paymentKey
     * - 아임포트: imp_uid
     * - 카카오페이: tid
     */
    @Column(name = "PAYMENT_KEY", length = 200)
    private String paymentKey;

    /**
     * 주문 고유번호
     * 우리 시스템에서 생성하는 주문 식별자 (보통 UUID)
     * PG사에 전달하는 orderId 역할
     * 중복되면 안 되므로 UNIQUE 제약조건 필요
     */
    @Column(name = "ORDER_UUID", nullable = false, length = 100, unique = true)
    private String orderUuid;

    /**
     * 결제 금액
     * 실제 결제 요청한 금액 (원 단위)
     */
    @Column(name = "PAYMENT_AMOUNT", nullable = false)
    private Integer paymentAmount;

    /**
     * 결제 수단
     * 카드, 계좌이체, 가상계좌 등
     * PG사별로 지원하는 결제수단이 다를 수 있음
     */
    @Column(name = "PAYMENT_METHOD", length = 30)
    private String paymentMethod;

    /**
     * 결제 상태
     * 전략패턴 실행 결과를 나타내는 중요한 필드
     */
    @Column(name = "PAYMENT_STATUS", nullable = false, length = 30)
    private String paymentStatus;

    /**
     * PG사별 응답 데이터
     * 각 PG사에서 반환하는 원본 응답을 JSON으로 저장
     * 디버깅 및 추가 정보 확인용
     * - 토스페이먼츠: 카드사 정보, 승인번호 등
     * - 아임포트: PG사별 상세 응답 등
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "PG_RESPONSE_DATA", columnDefinition = "JSON")
    private JsonNode pgResponseData;

    /**
     * 에러 코드
     * 결제 실패 시 PG사에서 반환하는 에러 코드
     */
    @Column(name = "ERROR_CODE", length = 50)
    private String errorCode;

    /**
     * 에러 메시지
     * 결제 실패 시 사용자에게 보여줄 에러 메시지
     */
    @Column(name = "ERROR_MESSAGE", length = 255)
    private String errorMessage;

    /**
     * 생성 시간
     * 결제 요청이 생성된 시간
     */
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     * 결제 상태가 변경된 시간 (마지막 업데이트)
     */
    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID")
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

}
