# 실시간 경매 플랫폼 도메인 모델

## 핵심 도메인 개념

### 사용자 (User)
- 플랫폼 사용자를 나타내는 엔티티
- 구매자(BUYER)와 판매자(SELLER) 역할 구분
- 이메일을 통한 고유 식별

### 상품 (Product)
- 경매에 출품되는 상품 정보
- 판매자에 의해 등록됨
- 상품 상태 관리 (판매 중, 판매 완료, 판매 취소 등)

### 경매 (Auction)
- 상품에 대한 경매 진행 정보
- 시작 시간, 종료 시간, 시작가 등 경매 규칙 포함
- 경매 상태 관리 (대기 중, 진행 중, 종료됨, 취소됨)

### 입찰 (Bid)
- 구매자가 경매에 참여한 입찰 정보
- 입찰 금액, 입찰 시간 등 포함
- 경매와 구매자 간의 관계 표현

## 도메인 모델 다이어그램

```
┌─────────────────┐       ┌─────────────────┐
│      User       │       │     Product     │
├─────────────────┤       ├─────────────────┤
│ id: Long        │       │ id: Long        │
│ email: Email    │◄──────┤ seller: User    │
│ name: String    │       │ name: String    │
│ role: Role      │       │ description: String
└─────────────────┘       │ status: ProductStatus
                          └─────────────────┘
                                  ▲
                                  │
                                  │
┌─────────────────┐       ┌─────────────────┐
│       Bid       │       │     Auction     │
├─────────────────┤       ├─────────────────┤
│ id: Long        │       │ id: Long        │
│ auction: Auction│◄──────┤ product: Product│
│ bidder: User    │       │ startingPrice: Money
│ amount: Money   │       │ startTime: LocalDateTime
│ bidTime: LocalDateTime  │ endTime: LocalDateTime
└─────────────────┘       │ status: AuctionStatus
                          │ highestBid: Money?
                          │ highestBidder: User?
                          └─────────────────┘
```

## Value Object

### Money
- 금액을 나타내는 값 객체
- 불변성 보장
- 금액 비교, 연산 기능 제공

### Email
- 이메일 주소를 나타내는 값 객체
- 유효성 검증 포함
- 불변성 보장

## 도메인 이벤트

### AuctionCreatedEvent
- 새로운 경매가 생성되었을 때 발행

### AuctionStartedEvent
- 경매가 시작되었을 때 발행

### AuctionEndedEvent
- 경매가 종료되었을 때 발행

### BidPlacedEvent
- 새로운 입찰이 발생했을 때 발행

## 도메인 규칙

### 사용자 관련 규칙
1. 이메일은 유효한 형식이어야 함
2. 이메일은 시스템 내에서 고유해야 함
3. 사용자는 구매자 또는 판매자 역할을 가짐

### 상품 관련 규칙
1. 상품은 판매자만 등록할 수 있음
2. 상품 이름은 필수이며 비어있을 수 없음
3. 판매 중인 상품만 경매에 등록 가능

### 경매 관련 규칙
1. 경매 시작 시간은 현재 시간보다 미래여야 함
2. 경매 종료 시간은 시작 시간보다 미래여야 함
3. 경매 시작가는 0보다 커야 함
4. 경매 상태는 시간에 따라 자동으로 업데이트됨
   - 시작 시간 이전: PENDING
   - 시작 시간 ~ 종료 시간: ONGOING
   - 종료 시간 이후: ENDED

### 입찰 관련 규칙
1. 진행 중인 경매에만 입찰 가능
2. 입찰가는 현재 최고 입찰가보다 높아야 함
3. 첫 입찰의 경우 시작가보다 높아야 함
4. 판매자는 자신의 경매에 입찰할 수 없음
5. 입찰 시 경매의 최고 입찰가와 최고 입찰자가 업데이트됨

## 주요 유스케이스

### 상품 등록
```kotlin
// 판매자가 새로운 상품을 등록
fun registerProduct(sellerId: Long, productInfo: ProductInfo): Product {
    val seller = userRepository.findById(sellerId)
        .orElseThrow { UserException.notFound(sellerId) }
    
    require(seller.role == Role.SELLER) { "판매자만 상품을 등록할 수 있습니다" }
    
    val product = Product(
        seller = seller,
        name = productInfo.name,
        description = productInfo.description,
        status = ProductStatus.AVAILABLE
    )
    
    return productRepository.save(product)
}
```

### 경매 생성
```kotlin
// 판매자가 상품에 대한 경매 생성
fun createAuction(
    sellerId: Long, 
    productId: Long, 
    startingPrice: Money,
    startTime: LocalDateTime,
    endTime: LocalDateTime
): Auction {
    val seller = userRepository.findById(sellerId)
        .orElseThrow { UserException.notFound(sellerId) }
    
    val product = productRepository.findById(productId)
        .orElseThrow { ProductException.notFound(productId) }
    
    require(product.seller.id == seller.id) { "자신의 상품만 경매에 등록할 수 있습니다" }
    require(product.status == ProductStatus.AVAILABLE) { "판매 가능한 상품만 경매에 등록할 수 있습니다" }
    
    val auction = Auction(
        product = product,
        startingPrice = startingPrice,
        startTime = startTime,
        endTime = endTime,
        status = determineInitialStatus(startTime)
    )
    
    val savedAuction = auctionRepository.save(auction)
    eventPublisher.publish(AuctionCreatedEvent(savedAuction))
    
    return savedAuction
}
```

### 입찰 처리
```kotlin
// 구매자가 경매에 입찰
fun placeBid(auctionId: Long, bidderId: Long, amount: Money): Bid {
    val auction = auctionRepository.findById(auctionId)
        .orElseThrow { AuctionException.notFound(auctionId) }
    
    val bidder = userRepository.findById(bidderId)
        .orElseThrow { UserException.notFound(bidderId) }
    
    // 경매 상태 확인
    require(auction.status == AuctionStatus.ONGOING) { "진행 중인 경매에만 입찰할 수 있습니다" }
    
    // 판매자 자신의 경매 입찰 방지
    require(auction.product.seller.id != bidder.id) { "자신의 경매에는 입찰할 수 없습니다" }
    
    // 입찰가 검증
    val currentHighestBid = auction.highestBid ?: auction.startingPrice
    require(amount > currentHighestBid) { "입찰가는 현재 최고 입찰가보다 높아야 합니다" }
    
    // 입찰 생성 및 경매 정보 업데이트
    val bid = Bid(
        auction = auction,
        bidder = bidder,
        amount = amount,
        bidTime = LocalDateTime.now()
    )
    
    val savedBid = bidRepository.save(bid)
    
    // 경매 정보 업데이트
    auction.updateHighestBid(bidder, amount)
    auctionRepository.save(auction)
    
    // 이벤트 발행
    eventPublisher.publish(BidPlacedEvent(savedBid))
    
    return savedBid
}
```

### 경매 종료 처리
```kotlin
// 경매 종료 시간에 도달했을 때 처리
fun endAuction(auctionId: Long) {
    val auction = auctionRepository.findById(auctionId)
        .orElseThrow { AuctionException.notFound(auctionId) }
    
    if (auction.status != AuctionStatus.ONGOING) {
        return
    }
    
    if (LocalDateTime.now().isBefore(auction.endTime)) {
        return
    }
    
    auction.end()
    auctionRepository.save(auction)
    
    // 상품 상태 업데이트
    val product = auction.product
    product.updateStatus(ProductStatus.SOLD)
    productRepository.save(product)
    
    // 이벤트 발행
    eventPublisher.publish(AuctionEndedEvent(auction))
}
```

## 동시성 제어 전략

### 낙관적 락 (Optimistic Lock)
- 경매 엔티티에 버전 필드 추가
- 동시 입찰 시 충돌 감지 및 재시도 로직 구현

```kotlin
@Entity
class Auction(
    // ... 다른 필드
    
    @Version
    val version: Long = 0
)
```

### 비관적 락 (Pessimistic Lock)
- 높은 동시성이 예상되는 인기 경매의 경우 비관적 락 적용

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Auction a WHERE a.id = :auctionId")
fun findByIdWithPessimisticLock(auctionId: Long): Optional<Auction>
```

## 이벤트 기반 아키텍처

### 이벤트 정의
```kotlin
interface DomainEvent {
    val occurredAt: LocalDateTime
}

data class BidPlacedEvent(
    val bid: Bid,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : DomainEvent
```

### 이벤트 핸들러
```kotlin
@Component
class BidPlacedEventHandler(
    private val webSocketService: WebSocketService
) : DomainEventHandler<BidPlacedEvent> {
    
    override fun handle(event: BidPlacedEvent) {
        // 실시간 입찰 정보를 웹소켓을 통해 클라이언트에게 전송
        webSocketService.sendBidUpdate(event.bid)
    }
}
``` 