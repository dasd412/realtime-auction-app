# 실시간 경매 플랫폼

실시간 경매 플랫폼은 웹소켓을 활용한 실시간 입찰 기능, 동시성 제어, 그리고 DDD(Domain-Driven Design) 패턴을 학습하기 위한 토이 프로젝트입니다.

## 프로젝트 소개

이 프로젝트는 다음과 같은 기술적 목표를 가지고 있습니다.

1. 웹소켓을 통한 실시간 입찰 기능 구현
2. 동시성 제어 기법 구현 및 비교
3. Redis를 활용한 사용자 인증 및 토큰 관리
4. 데이터베이스 복제/파티셔닝 실험
5. DDD(Domain-Driven Design) 패턴 적용
6. 배치 처리 구현 (경매 마감, 결제 처리, 통계 생성)

## 패키지 구조

이 프로젝트는 DDD(Domain-Driven Design) 원칙에 따라 구성되었으며, 다음과 같은 패키지 구조를 가집니다.

```
src/main/kotlin/com/auctionapp/
├── domain/                  # 도메인 레이어
│   ├── service/             # 도메인 서비스
│   ├── vo/                  # 밸류 오브젝트
│   ├── entity/              # 도메인 엔티티
│   └── event/               # 도메인 이벤트
├── application/             # 애플리케이션 레이어
│   ├── service/             # 애플리케이션 서비스
│   ├── controller/          # 컨트롤러
│   ├── dto/                 # 데이터 전송 객체
│   └── scheduler/           # 스케줄러 (배치 작업)
├── infrastructure/          # 인프라스트럭처 레이어
│   ├── config/              # 설정 클래스
│   ├── persistence/         # 레포지토리 구현체 (RDBMS, Redis)
│   ├── security/            # 보안 관련 구현체
│   ├── websocket/           # 웹소켓 구현체
```

### 레이어별 역할 및 특징

#### 1. 도메인 레이어 (Domain Layer)

**역할**: 비즈니스 도메인 모델과 핵심 비즈니스 로직을 담당합니다.

**특징**:
- 외부 의존성이 최소화됨 (프레임워크, 데이터베이스 등에 의존하지 않음)
- 도메인 규칙과 제약조건을 표현
- 비즈니스 용어를 코드로 표현

**하위 패키지**:
- **service/**: 도메인 서비스
- **vo/**: 밸류 오브젝트
- **entity/**: 도메인 엔티티 클래스
- **event/**: 도메인 이벤트

#### 2. 애플리케이션 레이어 (Application Layer)

**역할**: 도메인 레이어와 외부 세계 사이의 조정을 담당합니다.

**특징**:
- 트랜잭션 관리
- 도메인 객체의 조합과 조정
- 보안 및 권한 검사
- 외부 시스템과의 통합

**하위 패키지**:
- **service/**: 애플리케이션 서비스
- **controller/**: API 엔드포인트 컨트롤러
- **scheduler/**: 배치 작업 스케줄러
- **dto/**: 데이터 전송 객체

#### 3. 인프라스트럭처 레이어 (Infrastructure Layer)

**역할**: 기술적 세부 구현을 담당합니다.

**특징**:
- 데이터베이스 접근
- 외부 API 통합
- 메시징 시스템
- 보안 구현
- 프레임워크 통합

**하위 패키지**:
- **config/**: 설정 클래스
- **persistence/**: 레포지토리 구현체 (RDBMS,Redis)
- **security/**: 보안 관련 구현체
- **websocket/**: 웹소켓 구현체

## 동시성 제어 전략
이 프로젝트는 동시성 제어를 위한 여러 전략을 구현하고 비교합니다.
전략 패턴을 통해 런타임에 동시성 제어 전략을 전환할 수 있도록 구현하여 각 전략의 성능과 확장성을 비교합니다.

## 개발 환경 설정

### 필수 요구사항
- JDK 17 이상
- Gradle 8.0 이상
- Docker (Redis 및 MySQL 컨테이너 실행용)

### 로컬 개발 환경 설정
1. 저장소 클론
   ```bash
   git clone https://github.com/yourusername/realtime-auction-app.git
   cd realtime-auction-app
   ```

2. 애플리케이션 실행
   ```bash
   ./gradlew bootRun
   ```

3. 웹 인터페이스 접속
   ```
   http://localhost:8080
   ```

## 테스트

다양한 테스트 방법을 제공합니다:

### 단위 테스트
```bash
./gradlew test
```

### 통합 테스트
```bash
./gradlew integrationTest
```

### 부하 테스트
JMeter 스크립트를 제공하여 동시성 제어 전략별 성능 테스트가 가능합니다.
```bash
./gradlew loadTest
```

***

실시간 경매 플랫폼 요구사항


1. 사용자 관리
* 사용자는 회원가입할 수 있다.
  * 이메일, 비밀번호, 이름을 입력하여 가입한다.
  * 이미 같은 이메일이 등록되어 있다면 status 409 (Conflict)를 반환한다.
  * 비밀번호는 8자 이상, 16자 이하 영문/숫자/특수문자를 포함해야 한다.
  * 회원가입 성공 시 status 201을 반환하고 사용자 정보(비밀번호 제외)를 응답한다.
* 사용자는 로그인할 수 있다.
  * 이메일과 비밀번호로 로그인한다.
  * 이메일이 존재하지 않거나 비밀번호가 일치하지 않으면 status 401을 반환한다.
  * 로그인 성공 시 JWT 토큰을 발급하고 status 200을 반환한다.
  * 토큰에는 사용자 ID, 이메일, 역할(ROLE)이 포함된다.
  * 토큰 유효 기간은 24시간이다.
* 사용자는 토큰을 갱신할 수 있다.
  * 만료된 토큰으로 갱신 요청 시 새로운 토큰을 발급한다.
  * 유효하지 않은 토큰으로 요청 시 status 401을 반환한다.
  * 인증되지 않은 사용자의 요청은 status 401을 반환한다.
2. 상품 관리
* 사용자는 상품을 등록할 수 있다.
  * 상품명, 설명, 이미지 URL을 입력해 등록한다.
  * 인증되지 않은 사용자의 요청은 status 401을 반환한다.
  * 상품명은 3자 이상 100자 이하여야 한다.
  * 등록 성공 시 status 201과 상품 ID를 반환한다.
* 사용자는 상품 목록을 조회할 수 있다.
  * 페이지네이션을 지원한다(기본 페이지 크기: 20개).
  * 상품명으로 필터링할 수 있다.
  * 최신순으로 정렬한다.
  * 인증 없이 접근 가능하다.
* 사용자는 상품 상세 정보를 조회할 수 있다.
  * 상품 ID로 상세 정보를 조회한다.
  * 존재하지 않는 상품 ID로 요청 시 status 404를 반환한다.
  * 인증 없이 접근 가능하다.
* 판매자는 자신의 상품을 수정할 수 있다.
  * 상품명, 설명,이미지 URL을 수정할 수 있다.
  * 인증되지 않은 사용자의 요청은 status 401을 반환한다.
  * 본인의 상품이 아닌 경우 status 403을 반환한다.
  * 이미 경매가 시작된 상품은 수정할 수 없으며 status 400을 반환한다.
* 판매자는 자신의 상품을 삭제할 수 있다.
  * 인증되지 않은 사용자의 요청은 status 401을 반환한다.
  * 본인의 상품이 아닌 경우 status 403을 반환한다.
  * 이미 경매가 시작된 상품은 삭제할 수 없으며 status 400을 반환한다.
  * 삭제 성공 시 status 204를 반환한다.
3. 경매 관리
* 판매자는 상품에 대한 경매를 등록할 수 있다.
  * 시작 시간, 종료 시간, 초기 가격, 최소 입찰 단위를 설정한다.
  * 초기 가격은 1,000원 이상이어야 한다.
  * 인증되지 않은 사용자의 요청은 status 401을 반환한다.
  * 본인의 상품이 아닌 경우 status 403을 반환한다.
  * 이미 경매가 등록된 상품은 다시 등록할 수 없으며 status 400을 반환한다.
  * 종료 시간은 시작 시간보다 최소 1시간 이후여야 한다.
  * 등록 성공 시 status 201과 경매 ID를 반환한다.
* 사용자는 경매 목록을 조회할 수 있다.
  * 진행 중/예정/종료된 경매로 필터링할 수 있다.
  * 페이지네이션을 지원한다(기본 페이지 크기: 20개).
  * 시작 시간순, 인기순(입찰 수)으로 정렬할 수 있다.
  * 인증 없이 접근 가능하다.
* 사용자는 경매 상세 정보를 조회할 수 있다.
  * 경매 ID로 상세 정보를 조회한다.
  * 존재하지 않는 경매 ID로 요청 시 status 404를 반환한다.
  * 경매 상세 정보에는 상품 정보, 현재 최고 입찰가, 입찰 횟수가 포함된다.
  * 인증 없이 접근 가능하다.
* 판매자는 자신의 경매를 취소할 수 있다.
  * 인증되지 않은 사용자의 요청은 status 401을 반환한다.
  * 본인의 경매가 아닌 경우 status 403을 반환한다.
  * 이미 시작된 경매는 취소할 수 없으며 status 400을 반환한다.
  * 취소 성공 시 status 204를 반환한다.
4. 입찰 기능
* 사용자는 경매에 입찰할 수 있다.
  * 입찰 금액을 입력하여 입찰한다.
  * 인증되지 않은 사용자의 요청은 status 401을 반환한다.
  * 자신의 상품에는 입찰할 수 없으며 status 400을 반환한다.
  * 진행 중이 아닌 경매에는 입찰할 수 없으며 status 400을 반환한다.
  * 현재 최고 입찰가보다 최소 입찰 단위 이상 높은 금액으로 입찰해야 한다.
  * 이 조건을 만족하지 않으면 status 400을 반환한다.
  * 입찰 성공 시 status 201과 입찰 ID를 반환한다.
  * 동시에 같은 경매에 입찰이 발생하면 선착순으로 처리한다.
* 사용자는 경매의 입찰 내역을 조회할 수 있다.
  * 경매 ID로 해당 경매의 입찰 내역을 조회한다.
  * 페이지네이션을 지원한다(기본 페이지 크기: 20개).
  * 인증 없이 접근 가능하다.
  * 입찰 내역에는 입찰자 정보(이름), 입찰 금액, 입찰 시간이 포함된다.
* 사용자는 자신의 입찰 내역을 조회할 수 있다.
  * 인증된 사용자만 자신의 입찰 내역을 조회할 수 있다.
  * 페이지네이션을 지원한다(기본 페이지 크기: 20개).
  * 최신순으로 정렬된다.
5. 실시간 기능
* 사용자는 경매의 실시간 정보를 받을 수 있다.
  * WebSocket을 통해 경매 정보 업데이트를 구독할 수 있다.
  * 새로운 입찰이 발생하면 실시간으로 알림을 받는다.
  * 경매 상태 변경(시작, 종료)시 실시간으로 알림을 받는다.
* 사용자는 실시간으로 입찰할 수 있다.
  * WebSocket을 통해 입찰 메시지를 전송할 수 있다.
  * 입찰 성공/실패 결과를 실시간으로 받을 수 있다.
6. 경매 자동화
* 시스템은 예정된 경매를 자동으로 시작한다.
  * 설정된 시작 시간에 경매 상태를 "ACTIVE"로 변경한다.
  * 경매 시작 시 해당 경매의 구독자에게 알림을 전송한다.
  * 시스템은 종료 시간이 지난 경매를 자동으로 종료한다.
  * 설정된 종료 시간에 경매 상태를 "ENDED"로 변경한다.
  * 최고 입찰자가 있는 경우 해당 상품의 상태를 "SOLD"로 변경한다.
  * 최고 입찰자가 없는 경우 상품의 상태를 "AVAILABLE"로 유지한다.
  * 경매 종료 시 해당 경매의 구독자에게 알림을 전송한다.
7. 동시성 제어
* 시스템은 동시 입찰을 처리할 수 있다.

## 테스트 전략

이 프로젝트는 각 레이어별로 명확한 테스트 전략을 가지고 있으며, 이를 통해 코드의 품질과 신뢰성을 보장합니다.

### 1. 도메인 레이어 테스트

**목적**: 핵심 비즈니스 로직과 도메인 규칙이 올바르게 동작하는지 검증

**테스트 대상**:
- 엔티티의 유효성 검증 로직
- 도메인 규칙과 제약 조건
- 밸류 오브젝트의 불변성과 동등성

**테스트 방식**:
- 단위 테스트 위주
- 외부 의존성 없이 독립적으로 실행
- 경계 조건과 예외 상황에 중점

**예시**:
- Product 엔티티의 이름 길이 제약 조건 검증
- Money 밸류 오브젝트의 연산 및 비교 로직 검증
- Auction 엔티티의 상태 전이 규칙 검증

### 2. 리포지토리 레이어 테스트

**목적**: 데이터 접근 로직이 올바르게 동작하는지 검증

**테스트 대상**:
- 커스텀 리포지토리 메서드
- 쿼리 실행 결과
- N+1 문제 발생 여부

**테스트 방식**:
- 인메모리 데이터베이스(H2) 사용
- JPA 로그 확인을 통한 쿼리 검증
- 실제 데이터베이스와 유사한 환경 구성

**예시**:
- findByNameContainingOrderByIdDesc의 정렬 및 필터링 검증
- findAllByOrderByIdDesc의 페이징 처리 검증
- 연관 관계가 있는 엔티티 조회 시 N+1 문제 발생 여부 확인

### 3. 서비스 레이어 테스트

**목적**: 비즈니스 흐름과 트랜잭션 관리가 올바르게 동작하는지 검증

**테스트 대상**:
- 애플리케이션 서비스의 비즈니스 로직
- 예외 처리 및 에러 시나리오
- 트랜잭션 경계

**테스트 방식**:
- Mock 객체를 활용한 단위 테스트
- 의존성 주입을 통한 컴포넌트 격리
- 다양한 시나리오에 대한 검증

**예시**:
- 상품 등록 시 유효성 검증 및 저장 로직 검증
- 상품 수정/삭제 시 권한 검증 및 예외 처리 검증
- 경매 진행 중인 상품에 대한 수정/삭제 제한 검증

### 4. 컨트롤러 레이어 테스트

**목적**: API 엔드포인트가 올바르게 요청을 처리하고 응답하는지 검증

**테스트 대상**:
- 요청 파라미터 바인딩 및 유효성 검증
- 인증/인가 처리
- HTTP 상태 코드 및 응답 본문

**테스트 방식**:
- MockMvc를 활용한 통합 테스트
- 서비스 레이어 Mock 처리
- HTTP 요청/응답 검증

**예시**:
- 상품 등록 API의 요청 파라미터 검증 및 응답 상태 코드 검증
- 상품 목록 조회 API의 페이징 및 필터링 파라미터 처리 검증
- 인증이 필요한 API에 대한 인증 헤더 검증

### 5. 통합 테스트

**목적**: 여러 컴포넌트가 함께 동작할 때의 시스템 동작 검증

**테스트 대상**:
- 엔드 투 엔드 시나리오
- 컴포넌트 간 상호작용
- 실제 환경과 유사한 조건에서의 동작

**테스트 방식**:
- 실제 의존성을 사용한 SpringBootTest
- 테스트 컨테이너를 활용한 외부 시스템 통합
- 주요 사용자 시나리오 기반 테스트

**예시**:
- 상품 등록부터 경매 생성, 입찰까지의 전체 흐름 검증
- 동시 입찰 상황에서의 동시성 제어 검증
- 웹소켓을 통한 실시간 입찰 알림 검증

### 6. 성능 테스트

**목적**: 시스템의 성능 및 확장성 검증

**테스트 대상**:
- 동시 사용자 처리 능력
- 응답 시간 및 처리량
- 동시성 제어 전략 비교

**테스트 방식**:
- JMeter를 활용한 부하 테스트
- 다양한 동시성 제어 전략 비교 (낙관적 락 vs 비관적 락)
- 실제 운영 환경과 유사한 조건 구성

**예시**:
- 동시에 100명이 같은 경매에 입찰하는 시나리오 테스트
- 낙관적 락과 비관적 락의 성능 및 사용자 경험 비교
- 대규모 경매 목록 조회 시 응답 시간 측정

