package com.auctionapp.application.controller

import com.auctionapp.TestSecurityConfig
import com.auctionapp.application.dto.request.PlaceBidRequest
import com.auctionapp.application.dto.request.RegisterAuctionRequest
import com.auctionapp.application.exception.NotFoundAuctionException
import com.auctionapp.application.exception.NotFoundProductException
import com.auctionapp.application.exception.UnauthorizedException
import com.auctionapp.application.service.AuctionAppService
import com.auctionapp.application.service.AuctionSortType
import com.auctionapp.com.auctionapp.application.controller.AuctionController
import com.auctionapp.com.auctionapp.expriment.concurrency.ConcurrencyControlStrategyRegistry
import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.AuctionStatus
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.exception.CannotCancelActiveAuctionException
import com.auctionapp.domain.exception.InvalidBidException
import com.auctionapp.domain.exception.UnAuthorizedCancelAuctionException
import com.auctionapp.domain.vo.Money
import com.auctionapp.expriment.concurrency.strategy.BidConflictException
import com.auctionapp.infrastructure.persistence.AuctionDetail
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@Import(TestSecurityConfig::class)
@WebMvcTest(controllers = [AuctionController::class])
class AuctionControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var auctionAppService: AuctionAppService

    @MockBean
    private lateinit var strategyRegistry: ConcurrencyControlStrategyRegistry

    @Test
    @DisplayName("경매 등록 성공 테스트")
    fun registerAuctionSuccess() {
        // given
        val request =
            RegisterAuctionRequest(
                productId = 1L,
                initialPrice = 10000L,
                minimumBidUnit = 1000L,
                startTime = LocalDateTime.now().plusDays(1),
                endTime = LocalDateTime.now().plusDays(2),
            )
        val auctionId = 1L

        given(
            auctionAppService.registerAuction(
                productId = request.productId,
                initialPrice = request.initialPrice,
                minimumBidUnit = request.minimumBidUnit,
                startTime = request.startTime,
                endTime = request.endTime,
            ),
        ).willReturn(auctionId)

        // when & then
        mockMvc.perform(
            post("/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.auctionId").value(auctionId))
    }

    @Test
    @DisplayName("경매 등록 실패 - 유효하지 않은 상품")
    fun registerAuctionFailInvalidProduct() {
        // given
        val request =
            RegisterAuctionRequest(
                productId = 999L,
                initialPrice = 10000L,
                minimumBidUnit = 1000L,
                startTime = LocalDateTime.now().plusDays(1),
                endTime = LocalDateTime.now().plusDays(2),
            )

        given(
            auctionAppService.registerAuction(
                productId = request.productId,
                initialPrice = request.initialPrice,
                minimumBidUnit = request.minimumBidUnit,
                startTime = request.startTime,
                endTime = request.endTime,
            ),
        ).willThrow(NotFoundProductException())

        // when & then
        mockMvc.perform(
            post("/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    @DisplayName("경매 목록 조회 테스트")
    fun getAuctionListSuccess() {
        // given
        val auction1 = mockAuction(1L, "경매 1")
        val auction2 = mockAuction(2L, "경매 2")
        val auctions = listOf(auction1, auction2)
        val page = PageImpl(auctions, PageRequest.of(0, 20), 2)

        given(auctionAppService.getAuctionList(AuctionStatus.ACTIVE, AuctionSortType.NONE, 0)).willReturn(page)

        // when & then
        mockMvc.perform(
            get("/auctions")
                .param("status", "ACTIVE")
                .param("sortType", "NONE")
                .param("pageNumber", "0"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.auctions.length()").value(2))
    }

    @Test
    @DisplayName("경매 상세 조회 성공 테스트")
    fun getAuctionDetailSuccess() {
        // given
        val auctionId = 1L
        val auctionDetail = mock(AuctionDetail::class.java)
        val auction = mockAuction(auctionId, "테스트 경매")

        given(auctionDetail.getAuction()).willReturn(auction)
        given(auctionDetail.getBidCount()).willReturn(5L)
        given(auctionDetail.getHighestBidAmount()).willReturn(15000L)

        given(auctionAppService.getAuctionDetail(auctionId)).willReturn(auctionDetail)

        // when & then
        mockMvc.perform(
            get("/auctions/{auctionId}", auctionId),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.auctionId").value(auctionId))
    }

    @Test
    @DisplayName("존재하지 않는 경매 상세 조회 실패 테스트")
    fun getAuctionDetailNotFound() {
        // given
        val auctionId = 999L

        given(auctionAppService.getAuctionDetail(auctionId)).willThrow(NotFoundAuctionException())

        // when & then
        mockMvc.perform(
            get("/auctions/{auctionId}", auctionId),
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    @DisplayName("경매 취소 성공 테스트")
    fun cancelAuctionSuccess() {
        // given
        val auctionId = 1L

        doNothing().`when`(auctionAppService).cancelAuction(auctionId)

        // when & then
        mockMvc.perform(
            delete("/auctions/{auctionId}", auctionId),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.auctionId").value(auctionId))
    }

    @Test
    @DisplayName("경매 취소 실패 - 권한 없음")
    fun cancelAuctionUnauthorized() {
        // given
        val auctionId = 2L

        doThrow(UnAuthorizedCancelAuctionException()).`when`(auctionAppService).cancelAuction(auctionId)

        // when & then
        mockMvc.perform(
            delete("/auctions/{auctionId}", auctionId),
        )
            .andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
    }

    @Test
    @DisplayName("경매 취소 실패 - 이미 진행 중인 경매")
    fun cancelAuctionAlreadyActive() {
        // given
        val auctionId = 3L

        doThrow(CannotCancelActiveAuctionException()).`when`(auctionAppService).cancelAuction(auctionId)

        // when & then
        mockMvc.perform(
            delete("/auctions/{auctionId}", auctionId),
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    @DisplayName("입찰 성공 테스트")
    fun placeBidSuccess() {
        // given
        val auctionId = 1L
        val request = PlaceBidRequest(amount = 15000L)
        val bidId = 1L

        given(auctionAppService.placeBid(auctionId, request.amount)).willReturn(bidId)

        // when & then
        mockMvc.perform(
            post("/auctions/{auctionId}/bids", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.bidId").value(bidId))
    }

    @Test
    @DisplayName("입찰 실패 - 존재하지 않는 경매")
    fun placeBidAuctionNotFound() {
        // given
        val auctionId = 999L
        val request = PlaceBidRequest(amount = 15000L)

        given(auctionAppService.placeBid(auctionId, request.amount)).willThrow(NotFoundAuctionException())

        // when & then
        mockMvc.perform(
            post("/auctions/{auctionId}/bids", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    @DisplayName("입찰 실패 - 잘못된 입찰 금액")
    fun placeBidInvalidAmount() {
        // given
        val auctionId = 1L
        val request = PlaceBidRequest(amount = 500L) // 너무 낮은 금액

        given(auctionAppService.placeBid(auctionId, request.amount)).willThrow(InvalidBidException())

        // when & then
        mockMvc.perform(
            post("/auctions/{auctionId}/bids", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    @DisplayName("입찰(Redis Lock) 성공 테스트")
    fun placeBidWithRedisLockSuccess() {
        // given
        val auctionId = 1L
        val request = PlaceBidRequest(amount = 15000L)
        val bidId = 1L

        given(auctionAppService.placeBidWithRedisLock(auctionId, request.amount)).willReturn(bidId)

        // when & then
        mockMvc.perform(
            post("/auctions/{auctionId}/bids/distributed", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.bidId").value(bidId))
    }

    @Test
    @DisplayName("입찰(Redis Lock) 실패 - 동시성 충돌")
    fun placeBidWithRedisLockConflict() {
        // given
        val auctionId = 1L
        val request = PlaceBidRequest(amount = 15000L)

        given(auctionAppService.placeBidWithRedisLock(auctionId, request.amount)).willThrow(BidConflictException())

        // when & then
        mockMvc.perform(
            post("/auctions/{auctionId}/bids/distributed", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
    }

    @Test
    @DisplayName("경매 입찰 내역 조회 테스트")
    fun getAuctionBidsSuccess() {
        // given
        val auctionId = 1L
        val bid1 = mockBid(1L, 10000L)
        val bid2 = mockBid(2L, 11000L)
        val bids = listOf(bid1, bid2)
        val page = PageImpl(bids, PageRequest.of(0, 20), 2)

        given(auctionAppService.getBidsOfAuction(auctionId, 0)).willReturn(page)

        // when & then
        mockMvc.perform(
            get("/auctions/{auctionId}/bids", auctionId)
                .param("pageNumber", "0"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bids.length()").value(2))
    }

    @Test
    @DisplayName("내가 생성한 경매 목록 조회 테스트")
    fun getMyAuctionsSuccess() {
        // given
        val auction1 = mockAuction(1L, "내 경매 1")
        val auction2 = mockAuction(2L, "내 경매 2")
        val auctions = listOf(auction1, auction2)
        val page = PageImpl(auctions, PageRequest.of(0, 20), 2)

        given(auctionAppService.getAuctionsOfAuctionOwner(0)).willReturn(page)

        // when & then
        mockMvc.perform(
            get("/auctions/my-auctions")
                .param("pageNumber", "0"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.auctions.length()").value(2))
    }

    @Test
    @DisplayName("내가 생성한 경매 목록 조회 실패 - 인증 안됨")
    fun getMyAuctionsUnauthorized() {
        // given
        given(auctionAppService.getAuctionsOfAuctionOwner(0)).willThrow(UnauthorizedException())

        // when & then
        mockMvc.perform(
            get("/auctions/my-auctions")
                .param("pageNumber", "0"),
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
    }

    @Test
    @DisplayName("내가 입찰한 경매 목록 조회 테스트")
    fun getMyBiddingAuctionsSuccess() {
        // given
        val auction1 = mockAuction(1L, "입찰 경매 1")
        val auction2 = mockAuction(2L, "입찰 경매 2")
        val auctions = listOf(auction1, auction2)
        val page = PageImpl(auctions, PageRequest.of(0, 20), 2)

        given(auctionAppService.getAuctionsOfBidder(0)).willReturn(page)

        // when & then
        mockMvc.perform(
            get("/auctions/my-bids/auctions")
                .param("pageNumber", "0"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.auctions.length()").value(2))
    }

    @Test
    @DisplayName("내 입찰 내역 조회 테스트")
    fun getMyBidsSuccess() {
        // given
        val bid1 = mockBid(1L, 10000L)
        val bid2 = mockBid(2L, 11000L)
        val bids = listOf(bid1, bid2)
        val page = PageImpl(bids, PageRequest.of(0, 20), 2)

        given(auctionAppService.getBidsOfUser(0)).willReturn(page)

        // when & then
        mockMvc.perform(
            get("/auctions/my-bids")
                .param("pageNumber", "0"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bids.length()").value(2))
    }

    @Test
    @DisplayName("현재 동시성 전략 조회 테스트")
    fun getCurrentStrategySuccess() {
        // given
        val strategyName = "pessimistic"
        given(strategyRegistry.getCurrentStrategyName()).willReturn(strategyName)

        // when & then
        mockMvc.perform(
            get("/auctions/admin/strategy"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.strategy").value(strategyName))
    }

    @Test
    @DisplayName("동시성 전략 변경 테스트")
    fun changeStrategySuccess() {
        // given
        val newStrategy = "semaphore"
        doNothing().`when`(strategyRegistry).setCurrentStrategy(newStrategy)
        given(strategyRegistry.getCurrentStrategyName()).willReturn(newStrategy)

        // when & then
        mockMvc.perform(
            post("/auctions/admin/strategy")
                .param("strategy", newStrategy),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.strategy").value(newStrategy))
    }

    // Mock 객체 생성 헬퍼 메서드
    private fun mockAuction(
        id: Long,
        productName: String,
    ): Auction {
        val user = mock(User::class.java)
        `when`(user.id).thenReturn(1L)
        `when`(user.name).thenReturn("사용자")

        val product = mock(Product::class.java)
        `when`(product.id).thenReturn(id)
        `when`(product.name).thenReturn(productName)
        `when`(product.imageUrl).thenReturn("https://example.com/image.jpg")
        `when`(product.user).thenReturn(user)

        val initialPrice = mock(Money::class.java)
        `when`(initialPrice.amount).thenReturn(10000L)

        val minimumBidUnit = mock(Money::class.java)
        `when`(minimumBidUnit.amount).thenReturn(1000L)

        val auction = mock(Auction::class.java)
        `when`(auction.id).thenReturn(id)
        `when`(auction.user).thenReturn(user)
        `when`(auction.product).thenReturn(product)
        `when`(auction.initialPrice).thenReturn(initialPrice)
        `when`(auction.minimumBidUnit).thenReturn(minimumBidUnit)
        `when`(auction.startTime).thenReturn(LocalDateTime.now().plusDays(1))
        `when`(auction.endTime).thenReturn(LocalDateTime.now().plusDays(2))
        `when`(auction.status).thenReturn(AuctionStatus.ACTIVE)
        `when`(auction.bids).thenReturn(mutableListOf())

        return auction
    }

    private fun mockBid(
        id: Long,
        amount: Long,
    ): Bid {
        val money = mock(Money::class.java)
        `when`(money.amount).thenReturn(amount)

        val user = mock(User::class.java)
        `when`(user.id).thenReturn(2L)
        `when`(user.name).thenReturn("입찰자")

        val auction = mockAuction(1L, "경매 상품")

        val bid = mock(Bid::class.java)
        `when`(bid.id).thenReturn(id)
        `when`(bid.amount).thenReturn(money)
        `when`(bid.user).thenReturn(user)
        `when`(bid.auction).thenReturn(auction)
        `when`(bid.createdAt).thenReturn(LocalDateTime.now())

        return bid
    }
}
