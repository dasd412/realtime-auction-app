# 실시간 경매 플랫폼 코딩 컨벤션

## 일반 원칙

- **가독성 우선**: 코드는 작성하는 시간보다 읽는 시간이 더 많음을 인지
- **일관성 유지**: 프로젝트 전체에서 동일한 스타일 유지
- **단순성 추구**: 불필요한 복잡성 제거
- **자체 문서화 코드**: 명확한 이름과 구조로 코드 자체가 문서 역할을 할 수 있도록 작성

## Kotlin 코딩 스타일

### 네이밍 컨벤션

- **클래스/인터페이스**: PascalCase 사용
  ```kotlin
  class AuctionService
  interface BidRepository
  ```

- **함수/변수**: camelCase 사용
  ```kotlin
  fun placeBid(auction: Auction, amount: Money)
  val highestBid: Money
  ```

- **상수**: UPPER_SNAKE_CASE 사용
  ```kotlin
  const val MAX_BID_AMOUNT = 1_000_000
  ```

- **패키지**: 모두 소문자, 밑줄 없이 사용
  ```kotlin
  package com.auctionapp.domain.service
  ```

### 코드 포맷팅

- 들여쓰기: 4칸 공백 사용
- 최대 줄 길이: 120자
- 중괄호: 같은 줄에서 시작
  ```kotlin
  fun example() {
      // 코드
  }
  ```

### 함수 및 클래스 구조

- 함수는 한 가지 작업만 수행
- 함수 길이는 20줄 이내로 제한
- 클래스는 단일 책임 원칙(SRP) 준수
- 생성자 인자는 필수 값을 먼저, 선택적 값을 나중에 배치

```kotlin
class Auction(
    val id: Long,
    val product: Product,
    val seller: User,
    val startingPrice: Money,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: AuctionStatus = AuctionStatus.PENDING,
    val highestBid: Money? = null,
    val highestBidder: User? = null
)
```

## 아키텍처 관련 컨벤션

### 레이어 간 의존성

- 상위 레이어에서 하위 레이어로만 의존성 허용
- 순환 의존성 금지
- 인터페이스를 통한 의존성 역전 원칙(DIP) 적용

### 패키지 구조

- 기능이 아닌 레이어로 최상위 패키지 구성
- 관련 클래스는 동일 패키지에 배치
- 패키지 깊이는 3-4 수준으로 제한

```
com.auctionapp.domain.entity
com.auctionapp.application.service
com.auctionapp.infrastructure.persistence
```

## 도메인 모델 컨벤션

### 엔티티

- 식별자(ID)를 통한 동등성 비교
- 불변성 선호
- 도메인 로직은 엔티티 내부에 캡슐화
- 생성자에서 유효성 검사 수행

```kotlin
@Entity
class User(
    @Id @GeneratedValue
    val id: Long = 0,
    
    @Embedded
    val email: Email,
    
    val name: String,
    
    @Enumerated(EnumType.STRING)
    val role: Role
) {
    init {
        require(name.isNotBlank()) { "이름은 비어있을 수 없습니다" }
    }
    
    // 도메인 로직
    fun canBid(auction: Auction): Boolean {
        return role == Role.BUYER && auction.seller.id != this.id
    }
}
```

### Value Object

- 값에 의한 동등성 비교
- 완전한 불변성 보장
- 자체 유효성 검증 포함
- 의미 있는 도메인 개념 표현

```kotlin
@Embeddable
class Money(
    val amount: BigDecimal
) {
    init {
        require(amount >= BigDecimal.ZERO) { "금액은 0 이상이어야 합니다" }
    }
    
    operator fun plus(other: Money): Money {
        return Money(this.amount + other.amount)
    }
    
    operator fun minus(other: Money): Money {
        val result = this.amount - other.amount
        require(result >= BigDecimal.ZERO) { "결과 금액은 0 이상이어야 합니다" }
        return Money(result)
    }
    
    operator fun compareTo(other: Money): Int {
        return this.amount.compareTo(other.amount)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Money
        
        return amount.compareTo(other.amount) == 0
    }
    
    override fun hashCode(): Int {
        return amount.hashCode()
    }
}
```

## 예외 처리 컨벤션

- 체크 예외 대신 언체크 예외 사용
- 도메인별 커스텀 예외 클래스 정의
- 예외 메시지는 명확하고 구체적으로 작성
- 예외는 적절한 레벨에서 처리

```kotlin
class BidException(message: String) : RuntimeException(message) {
    companion object {
        fun bidAmountTooLow(currentBid: Money, attemptedBid: Money): BidException {
            return BidException("입찰가(${attemptedBid.amount})는 현재 최고 입찰가(${currentBid.amount})보다 높아야 합니다")
        }
        
        fun auctionNotActive(): BidException {
            return BidException("진행 중인 경매에만 입찰할 수 있습니다")
        }
    }
}
```

## 테스트 코드 컨벤션

- 테스트 메소드명은 한글로 작성하여 가독성 향상
- given-when-then 패턴 사용
- 테스트 데이터는 명확하고 최소한으로 구성
- 테스트 간 의존성 제거

```kotlin
@Test
fun `경매 종료 시간이 지나면 상태가 ENDED로 변경된다`() {
    // given
    val pastEndTime = LocalDateTime.now().minusDays(1)
    val auction = Auction(
        id = 1L,
        product = Product(...),
        seller = User(...),
        startingPrice = Money(1000),
        startTime = pastEndTime.minusDays(1),
        endTime = pastEndTime,
        status = AuctionStatus.ONGOING
    )
    
    // when
    auction.checkAndUpdateStatus()
    
    // then
    assertThat(auction.status).isEqualTo(AuctionStatus.ENDED)
}
```

## 주석 컨벤션

- 코드로 표현할 수 있는 내용은 주석 대신 코드 자체를 명확히 작성
- 복잡한 알고리즘이나 비즈니스 로직에만 주석 사용
- KDoc 형식 사용하여 API 문서화
- TODO, FIXME 주석은 이슈 트래커 참조 포함

```kotlin
/**
 * 경매에 입찰을 시도합니다.
 * 
 * @param bidder 입찰자
 * @param amount 입찰 금액
 * @throws BidException 입찰 금액이 현재 최고 입찰가보다 낮거나, 경매가 진행 중이 아닌 경우
 * @return 생성된 입찰 정보
 */
fun placeBid(bidder: User, amount: Money): Bid {
    validateAuctionIsActive()
    validateBidAmount(amount)
    validateBidder(bidder)
    
    val bid = Bid(auction = this, bidder = bidder, amount = amount)
    updateHighestBid(bid)
    
    return bid
}
```

## 데이터베이스 관련 컨벤션

- 테이블명: 복수형, snake_case 사용
- 컬럼명: snake_case 사용
- 외래 키: `{참조테이블명}_id` 형식 사용
- 인덱스: `idx_{테이블명}_{컬럼명}` 형식 사용

```kotlin
@Entity
@Table(name = "auctions")
class Auction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Product,
    
    // ... 다른 필드
)
```

## 로깅 컨벤션

- 로그 레벨 적절히 사용
  - ERROR: 시스템 오류, 복구 불가능한 상황
  - WARN: 잠재적 문제, 자동 복구 가능한 상황
  - INFO: 중요 비즈니스 이벤트, 시스템 상태 변경
  - DEBUG: 개발 및 문제 해결용 상세 정보
- 구조화된 로깅 사용
- 민감 정보는 로깅하지 않음

```kotlin
private val log = LoggerFactory.getLogger(AuctionService::class.java)

fun endAuction(auctionId: Long) {
    log.info("경매 종료 시작: auctionId={}", auctionId)
    
    try {
        val auction = auctionRepository.findById(auctionId)
            .orElseThrow { AuctionException.notFound(auctionId) }
        
        auction.end()
        auctionRepository.save(auction)
        
        log.info("경매 종료 완료: auctionId={}, 최종 낙찰가={}", 
                auctionId, auction.highestBid?.amount)
    } catch (e: Exception) {
        log.error("경매 종료 실패: auctionId={}", auctionId, e)
        throw e
    }
}
``` 