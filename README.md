# 실시간 경매 플랫폼

실시간 경매 플랫폼은 웹소켓을 활용한 실시간 입찰 기능, 동시성 제어, 그리고 DDD(Domain-Driven Design) 패턴을 학습하기 위한 토이 프로젝트입니다.

## 프로젝트 소개

이 프로젝트는 다음과 같은 기술적 목표를 가지고 있습니다.

1. 웹소켓을 통한 실시간 입찰 기능 구현
2. 동시성 제어 기법 구현 및 비교
3. Redis를 활용한 사용자 인증 및 토큰 관리
4. 데이터베이스 샤딩/파티셔닝 실험
5. DDD(Domain-Driven Design) 패턴 적용
6. 배치 처리 구현 (경매 마감, 결제 처리, 통계 생성)

## 패키지 구조

이 프로젝트는 DDD(Domain-Driven Design) 원칙에 따라 구성되었으며, 다음과 같은 패키지 구조를 가집니다.

```
src/main/kotlin/com/auctionapp/
├── domain/                  # 도메인 레이어
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
