# Book Bot - 온라인 서점 시스템

Spring Boot 기반의 온라인 서점 시스템입니다. 네이버 도서 API를 활용한 도서 검색, 주문 관리, 재고 알림 시스템 등을 제공합니다.

## 기술 스택

- **Backend**: Spring Boot, Spring Security, Spring Data JPA
- **Database**: MySQL, Redis
- **External API**: Naver Book Search API
- **Communication**: WebClient
- **Authentication**: JWT
- **Email**: Spring Mail
- **Build Tool**: Gradle/Maven

## 주요 기능

### 1. 사용자 인증 시스템
- JWT 기반 사용자 인증
- Redis 세션 관리

### 2. 도서 관리
- 네이버 도서 API 연동
- 도서 검색 및 정보 조회
- 외부 API 데이터 MySQL 저장

### 3. 주문 시스템
- 주문 생성 및 관리
- 베스트셀러 집계

### 4. 등급 관리 (전략 패턴)
- 사용자 등급 시스템
- Redis 캐싱을 통한 성능 최적화
- 전략 패턴 기반 등급 처리

### 5. 재고 알림 시스템 (옵저버 패턴)
- 옵저버 패턴 기반 재고 관리
- 이메일 알림 기능

### 6. 예외 처리
- GlobalExceptionHandler를 통한 통합 예외 처리
- 커스텀 Exception 클래스
- 일관된 API 응답 구조

## 아키텍처

```
src/main/java/com/fastcampus/book_bot/
├── common/              # 공통 설정 및 유틸리티
│   ├── config/         # Spring 설정 클래스
│   ├── exception/      # 커스텀 예외 처리
│   ├── response/       # API 응답 구조
│   └── utils/          # 유틸리티 클래스
├── controller/         # REST 컨트롤러
│   ├── api/           # 외부 API 컨트롤러
│   ├── auth/          # 인증 관련 컨트롤러
│   ├── book/          # 도서 관련 컨트롤러
│   └── order/         # 주문 관련 컨트롤러
├── domain/            # JPA 엔티티
│   ├── book/          # 도서 엔티티
│   ├── noti/          # 알림 엔티티
│   ├── orders/        # 주문 엔티티
│   ├── payment/       # 결제 엔티티
│   └── user/          # 사용자 엔티티
├── repository/        # JPA Repository
├── service/          # 비즈니스 로직
│   ├── api/          # 외부 API 서비스
│   ├── auth/         # 인증 서비스
│   ├── book/         # 도서 서비스
│   ├── grade/        # 등급 서비스 (전략 패턴)
│   ├── noti/         # 알림 서비스 (옵저버 패턴)
│   └── order/        # 주문 서비스
└── dto/              # 데이터 전송 객체
```

## 디자인 패턴

### 전략 패턴 (Strategy Pattern)
- **위치**: `service/grade/`
- **목적**: 사용자 등급별 다른 처리 로직 구현
- **구성**: `GradeStrategy`, `GradeStrategyFactory`, `RedisGradeStrategy`

### 옵저버 패턴 (Observer Pattern)
- **위치**: `service/noti/`
- **목적**: 재고 변동 시 구독자들에게 알림 전송
- **구성**: `StockSubject`, `StockObserver`, `SubscriptionObserver`

## 실행 방법

### 환경 설정
```bash
# MySQL 및 Redis 실행 (Docker Compose 사용)
cd mysql-docker
docker-compose up -d
```

### API 테스트
- **Base URL**: `http://localhost:8080`
- **API 문서**: Swagger UI 또는 Postman 활용

## 설정 파일

### application.yml
```yaml
# 데이터베이스, Redis, 외부 API 설정
# JWT 토큰 설정
# 메일 서버 설정
```

## 개발 내용

- **도메인 중심 설계**: 각 도메인별로 명확한 책임 분리
- **디자인 패턴 적용**: 전략 패턴과 옵저버 패턴을 통한 유연한 설계
- **외부 API 연동**: WebClient를 활용한 비동기 통신
- **캐싱 전략**: Redis를 활용한 성능 최적화
- **보안**: JWT 기반 인증 및 Spring Security 적용
- **예외 처리**: 전역 예외 처리기를 통한 일관된 오류 응답
