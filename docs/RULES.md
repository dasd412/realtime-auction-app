# 실시간 경매 플랫폼 프로젝트 규칙

## 프로젝트 개요
- 실시간 경매 플랫폼 토이 프로젝트
- 목표: 웹소켓을 활용한 실시간 입찰, 동시성 제어, DDD 패턴 학습
- 주요 기술 스택: Spring Boot, Kotlin, JPA/JOOQ, WebSocket, JWT

## 아키텍처 규칙

### 패키지 구조 (DDD 기반)
```
com.auctionapp
├── domain
│   ├── entity       # 도메인 엔티티 (User, Product, Auction, Bid 등)
│   ├── vo           # Value Object (Email, Money 등)
│   ├── dto          # 도메인 내부 DTO
│   ├── service      # 도메인 서비스
│   ├── event        # 도메인 이벤트
│   └── exception    # 도메인 예외
├── application
│   ├── controller   # API 컨트롤러
│   ├── dto          # 요청/응답 DTO
│   ├── service      # 애플리케이션 서비스
│   ├── exception    # 애플리케이션 예외
│   └── scheduler    # 스케줄러
├── infrastructure
│   ├── config       # 설정 클래스
│   ├── persistence  # 리포지토리 구현체
│   ├── redis        # Redis 관련 구현 (JWT 토큰 세션 관리)
│   ├── security     # 보안 관련 구현
│   └── websocket    # 웹소켓 구현
└── util             # 유틸리티 클래스
```

### 레이어 간 의존성 규칙
- Domain → 외부 의존성 없음 (순수 도메인)
- Application → Domain 의존
- Infrastructure → Domain, Application 의존
- 상위 레이어는 하위 레이어에 의존할 수 없음

## 기술 스택 활용 규칙

### Redis
- 용도: JWT 토큰 세션 만료 관리
- 분산 락 사용하지 않음

### 동시성 제어
- JPA의 낙관적 락(Optimistic Lock) 활용
- 필요한 경우 비관적 락(Pessimistic Lock) 활용
- 다양한 동시성 제어 기법 실험 및 적용

### 웹소켓
- 실시간 입찰 정보 전달에 활용
- STOMP 프로토콜 사용

### 보안
- Spring Security + JWT 기반 인증/인가
- 토큰 만료 관리에 Redis 활용

## 코드 작성 규칙

### 엔티티 설계
- 모든 엔티티는 직접 구현
- 도메인 로직은 엔티티 내부에 캡슐화
- Value Object 패턴 적극 활용 (Email, Money 등)

### 예외 처리
- 도메인별 예외 클래스 분리
- 예외는 적절한 레이어에서 처리

### 테스트 코드
- 단위 테스트 필수 작성
- 레이어별 테스트 전략:
  - 도메인: 순수 비즈니스 로직 테스트
  - 리포지토리: 커스텀 쿼리 중심 테스트
  - 서비스: Mock 객체를 활용한 비즈니스 흐름 테스트
  - 컨트롤러: 응답 검증 중심 테스트

### 이벤트 처리
- 도메인 이벤트 패턴 활용
- 비동기 이벤트 처리를 통한 결합도 감소

## 프론트엔드 규칙
- HTML/CSS/JS 기반 간단한 UI
- Spring Boot 정적 리소스 디렉토리(src/main/resources/static)에 저장
- Spring Security 설정을 통한 정적 리소스 접근 허용

## CI/CD 규칙
- GitHub Actions 활용
- 코드 스타일 검사
- 단위/통합 테스트
- 코드 커버리지 측정
- 정적 코드 분석
- 빌드 확인
- Docker 이미지 빌드

## 로컬 개발 환경
- Docker Compose 활용
  - MySQL
  - Redis
  - Adminer
  - Redis Commander 