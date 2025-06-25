# 실시간 경매 플랫폼 테스트 전략

## 테스트 원칙

- **단위 테스트 우선**: 모든 레이어에서 단위 테스트를 기본으로 작성
- **레이어별 테스트 전략 차별화**: 각 레이어의 특성에 맞는 테스트 방식 적용
- **테스트 가독성 중시**: 테스트 코드는 문서화의 역할도 수행
- **테스트 커버리지 관리**: 핵심 비즈니스 로직은 높은 커버리지 유지

## 레이어별 테스트 전략

### 도메인 레이어 테스트

- **테스트 대상**: 엔티티, Value Object, 도메인 서비스
- **테스트 방식**: 순수 단위 테스트 (외부 의존성 없음)
- **테스트 도구**: JUnit 5, AssertJ
- **테스트 범위**: 모든 비즈니스 규칙과 제약조건 검증

```kotlin
@Test
fun `입찰가는 현재 최고 입찰가보다 높아야 한다`() {
    // given
    val auction = Auction(...)
    val currentHighestBid = Money(10000)
    auction.updateHighestBid(currentHighestBid)
    
    // when & then
    assertThrows<BidException> {
        auction.placeBid(User(...), Money(9000))
    }
}
```

### 리포지토리 레이어 테스트

- **테스트 대상**: 리포지토리 인터페이스 구현체
- **테스트 방식**: 데이터베이스 연동 테스트 (인메모리 DB 활용)
- **테스트 도구**: JUnit 5, Spring Boot Test, H2 Database
- **테스트 범위**: 커스텀 쿼리 중심으로 테스트

```kotlin
@DataJpaTest
class AuctionRepositoryTest {
    @Autowired
    private lateinit var auctionRepository: AuctionRepository
    
    @Test
    fun `진행 중인 경매 목록을 조회한다`() {
        // given
        val auction1 = Auction(status = AuctionStatus.ONGOING)
        val auction2 = Auction(status = AuctionStatus.ENDED)
        auctionRepository.saveAll(listOf(auction1, auction2))
        
        // when
        val result = auctionRepository.findAllByStatus(AuctionStatus.ONGOING)
        
        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].status).isEqualTo(AuctionStatus.ONGOING)
    }
}
```

### 애플리케이션 서비스 레이어 테스트

- **테스트 대상**: 애플리케이션 서비스
- **테스트 방식**: Mock 객체를 활용한 단위 테스트
- **테스트 도구**: JUnit 5, Mockito, MockK
- **테스트 범위**: 비즈니스 흐름 및 트랜잭션 검증

```kotlin
@ExtendWith(MockitoExtension::class)
class AuctionAppServiceTest {
    @Mock
    private lateinit var auctionRepository: AuctionRepository
    
    @Mock
    private lateinit var bidRepository: BidRepository
    
    @InjectMocks
    private lateinit var auctionAppService: AuctionAppService
    
    @Test
    fun `입찰 성공 시 최고 입찰가가 업데이트된다`() {
        // given
        val auction = mock(Auction::class.java)
        val user = mock(User::class.java)
        val bidAmount = Money(10000)
        
        `when`(auctionRepository.findById(anyLong())).thenReturn(Optional.of(auction))
        
        // when
        auctionAppService.placeBid(1L, user.id, bidAmount)
        
        // then
        verify(auction).placeBid(user, bidAmount)
        verify(auctionRepository).save(auction)
    }
}
```

### 컨트롤러 레이어 테스트

- **테스트 대상**: REST 컨트롤러, 웹소켓 컨트롤러
- **테스트 방식**: MockMvc를 활용한 단위 테스트
- **테스트 도구**: Spring Boot Test, MockMvc
- **테스트 범위**: API 응답 검증, 상태 코드, 응답 본문

```kotlin
@WebMvcTest(AuctionController::class)
class AuctionControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var auctionAppService: AuctionAppService
    
    @Test
    fun `입찰 API 호출 시 성공 응답이 반환된다`() {
        // given
        val bidRequest = BidRequest(auctionId = 1L, amount = 10000)
        
        // when & then
        mockMvc.perform(post("/api/auctions/bid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ObjectMapper().writeValueAsString(bidRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
}
```

## 테스트 데이터 관리

### 테스트 픽스처

- 공통 테스트 데이터는 픽스처로 관리
- 테스트 데이터 팩토리 패턴 활용

```kotlin
object UserFixture {
    fun createUser(
        id: Long = 1L,
        email: String = "test@example.com",
        name: String = "테스트 사용자"
    ): User {
        return User(id = id, email = Email(email), name = name)
    }
}
```

### 테스트 데이터베이스

- H2 인메모리 데이터베이스 활용
- 테스트 실행 시 스키마 자동 생성
- 테스트 간 데이터 격리

## 테스트 자동화

### CI/CD 파이프라인 통합

- GitHub Actions를 통한 자동 테스트 실행
- Pull Request 시 테스트 실행 필수
- 테스트 실패 시 빌드 실패 처리

### 테스트 커버리지 관리

- JaCoCo를 통한 테스트 커버리지 측정
- 도메인 레이어: 최소 90% 이상 커버리지 목표
- 애플리케이션 레이어: 최소 80% 이상 커버리지 목표
- 인프라스트럭처 레이어: 핵심 기능 위주 테스트

## 테스트 모범 사례

### 테스트 네이밍 컨벤션

- 백틱(`)을 활용한 한글 테스트 메소드명 사용
- `given-when-then` 형식의 주석 사용
- 테스트 설명은 구체적이고 명확하게 작성

### 테스트 코드 품질 관리

- 테스트 코드도 프로덕션 코드와 동일한 품질 기준 적용
- 중복 코드 제거 및 가독성 향상
- 테스트 유틸리티 클래스 활용 