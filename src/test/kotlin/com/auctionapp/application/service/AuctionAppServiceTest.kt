package com.auctionapp.application.service

import com.auctionapp.application.constant.DEFAULT_AUCTION_PAGE_SIZE
import com.auctionapp.application.exception.NotFoundAuctionException
import com.auctionapp.application.exception.NotFoundProductException
import com.auctionapp.application.exception.NotFoundUserException
import com.auctionapp.application.exception.UnauthorizedException
import com.auctionapp.com.auctionapp.expriment.concurrency.ConcurrencyControlStrategyRegistry
import com.auctionapp.com.auctionapp.utils.SecurityUtil
import com.auctionapp.domain.entity.*
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Email
import com.auctionapp.domain.vo.Money
import com.auctionapp.expriment.concurrency.strategy.ConcurrencyControlStrategy
import com.auctionapp.infrastructure.persistence.*
import com.auctionapp.infrastructure.scheduler.AuctionSchedulerService
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.redisson.api.RedissonClient
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

@SuppressWarnings("unused")
class AuctionAppServiceTest {
    private val auctionService = mockk<AuctionService>()
    private val auctionRepository = mockk<AuctionRepository>()
    private val bidRepository = mockk<BidRepository>()
    private val userRepository = mockk<UserRepository>()
    private val productRepository = mockk<ProductRepository>()
    private val strategyRegistry = mockk<ConcurrencyControlStrategyRegistry>()
    private val redissonClient = mockk<RedissonClient>()
    private val transactionManger = mockk<PlatformTransactionManager>()
    private val securityUtilMockObject = mockkObject(SecurityUtil) // 지우면 모킹 실패하니 지우지 마세요.
    private val auctionSchedulerService = mockk<AuctionSchedulerService>()

    private val auctionAppService =
        AuctionAppService(
            auctionService,
            auctionRepository,
            bidRepository,
            userRepository,
            productRepository,
            strategyRegistry,
            redissonClient,
            transactionManger,
            auctionSchedulerService,
        )

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 경매 등록을 시도할 경우 예외가 발생한다")
    fun registerAuction_unauthorizedUser() {
        // given
        val productId = 1L
        val initialPrice = 1000L
        val minimumBidUnit = 100L
        val startTime = LocalDateTime.now()
        val endTime = startTime.plusHours(1)
        every { SecurityUtil.getCurrentUsername() } returns null

        // when, then
        assertThrows<UnauthorizedException> {
            auctionAppService.registerAuction(productId, initialPrice, minimumBidUnit, startTime, endTime)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 경매 취소를 시도할 경우 예외가 발생한다")
    fun cancelAuction_unauthorizedUser() {
        // given
        val auctionId = 1L
        every { SecurityUtil.getCurrentUsername() } returns null

        // when, then
        assertThrows<UnauthorizedException> {
            auctionAppService.cancelAuction(auctionId)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 입찰을 시도할 경우 예외가 발생한다")
    fun placeBid_unauthorizedUser() {
        // given
        val auctionId = 1L
        val amount = 1000L
        every { SecurityUtil.getCurrentUsername() } returns null

        // when, then
        assertThrows<UnauthorizedException> {
            auctionAppService.placeBid(auctionId, amount)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 자신의 입찰 목록을 조회할 경우 예외가 발생한다")
    fun getBidsOfUser_unauthorizedUser() {
        // given
        val pageNumber = 0
        every { SecurityUtil.getCurrentUsername() } returns null

        // when, then
        assertThrows<UnauthorizedException> {
            auctionAppService.getBidsOfUser(pageNumber)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 자신이 등록한 경매 목록을 조회할 경우 예외가 발생한다")
    fun getAuctionsOfAuctionOwner_unauthorizedUser() {
        // given
        val pageNumber = 0
        every { SecurityUtil.getCurrentUsername() } returns null

        // when, then
        assertThrows<UnauthorizedException> {
            auctionAppService.getAuctionsOfAuctionOwner(pageNumber)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 자신이 참여한 경매 목록을 조회할 경우 예외가 발생한다")
    fun getAuctionsOfBidder_unauthorizedUser() {
        // given
        val pageNumber = 0
        every { SecurityUtil.getCurrentUsername() } returns null

        // when, then
        assertThrows<UnauthorizedException> {
            auctionAppService.getAuctionsOfBidder(pageNumber)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 경우 경매 등록에서 예외가 발생한다")
    fun registerAuction_userNotFound() {
        // given
        val productId = 1L
        val initialPrice = 1000L
        val minimumBidUnit = 100L
        val startTime = LocalDateTime.now()
        val endTime = startTime.plusHours(1)
        val email = "test@example.com"
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns null

        // when, then
        assertThrows<NotFoundUserException> {
            auctionAppService.registerAuction(productId, initialPrice, minimumBidUnit, startTime, endTime)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
    }

    @Test
    @DisplayName("상품이 존재하지 않을 경우 경매 등록에서 예외가 발생한다")
    fun registerAuction_productNotFound() {
        // given
        val userId = 1L
        val productId = 1L
        val initialPrice = 1000L
        val minimumBidUnit = 100L
        val startTime = LocalDateTime.now()
        val endTime = startTime.plusHours(1)
        val email = "test@example.com"
        val user = mockk<User>()
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByIdOrNull(productId) } returns null

        // when, then
        assertThrows<NotFoundProductException> {
            auctionAppService.registerAuction(productId, initialPrice, minimumBidUnit, startTime, endTime)
        }

        verify(exactly = 1) { productRepository.findByIdOrNull(productId) }
    }

    @Test
    @DisplayName("경매 등록에 성공한다")
    fun registerAuction_success() {
        // given
        val productId = 1L
        val auctionId = 1L
        val initialPrice = 1000L
        val minimumBidUnit = 100L
        val startTime = LocalDateTime.now()
        val endTime = startTime.plusHours(1)
        val email = "test@example.com"
        val user = mockk<User>()
        val product = mockk<Product>()
        val auction = mockk<Auction>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { auctionRepository.save(any()) } returns auction
        every { auction.id } returns auctionId
        every { auctionService.registerAuction(any(), any(), any()) } returns Unit
        every { auctionSchedulerService.scheduleAuctionJobs(auction) } returns Unit

        // when
        auctionAppService.registerAuction(productId, initialPrice, minimumBidUnit, startTime, endTime)

        // then
        verify { SecurityUtil.getCurrentUsername() }
        verify { userRepository.findByEmail(Email(email)) }
        verify { productRepository.findByIdOrNull(productId) }
        verify { auctionRepository.save(any()) }
    }

    @Test
    @DisplayName("경매 리스트 조회에 성공한다 (정렬 조건 없음)")
    fun getAuctionList_success() {
        // given
        val status = AuctionStatus.ACTIVE
        val sortType = AuctionSortType.NONE
        val pageNumber = 0

        val auction = mockk<Auction>()
        val auctionList = PageImpl(listOf(auction))

        every {
            auctionRepository.findByStatus(status, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
        } returns auctionList

        // when
        val result = auctionAppService.getAuctionList(status, sortType, pageNumber)

        // then
        assertThat(result).isEqualTo(auctionList)
        verify(
            exactly = 1,
        ) { auctionRepository.findByStatus(status, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE)) }
    }

    @Test
    @DisplayName("경매 리스트 조회에 성공한다 (시간 오름차순 정렬)")
    fun getAuctionList_success_timeAsc() {
        // given
        val status = AuctionStatus.ACTIVE
        val sortType = AuctionSortType.TIME_ASC
        val pageNumber = 0

        val auction = mockk<Auction>()
        val auctionList = PageImpl(listOf(auction))

        every {
            auctionRepository.findByStatusOrderByStartTimeAsc(status, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
        } returns auctionList

        // when
        val result = auctionAppService.getAuctionList(status, sortType, pageNumber)

        // then
        assertThat(result).isEqualTo(auctionList)
        verify(exactly = 1) {
            auctionRepository.findByStatusOrderByStartTimeAsc(
                status,
                PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
            )
        }
    }

    @Test
    @DisplayName("경매 리스트 조회에 성공한다 (인기순 정렬)")
    fun getAuctionList_success_popularity() {
        // given
        val status = AuctionStatus.ACTIVE
        val sortType = AuctionSortType.POPULARITY
        val pageNumber = 0

        val auction = mockk<Auction>()
        val auctionList = PageImpl(listOf(auction))

        every {
            auctionRepository.findByStatusOrderByBidsCountDesc(status, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
        } returns auctionList

        // when
        val result = auctionAppService.getAuctionList(status, sortType, pageNumber)

        // then
        assertThat(result).isEqualTo(auctionList)
        verify(exactly = 1) {
            auctionRepository.findByStatusOrderByBidsCountDesc(
                status,
                PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
            )
        }
    }

    @Test
    @DisplayName("경매 상세 정보를 성공적으로 조회한다")
    fun getAuctionDetail_success() {
        // given
        val auctionId = 1L
        val auctionDetail = mockk<AuctionDetail>()
        val auction = mockk<Auction>()

        every { auctionDetail.getAuction() } returns auction
        every { auctionDetail.getBidCount() } returns 3L
        every { auctionDetail.getHighestBidAmount() } returns 2500L

        every { auctionRepository.findAuctionDetailById(auctionId) } returns auctionDetail

        // when
        val result = auctionAppService.getAuctionDetail(auctionId)

        // then
        assertThat(result).isEqualTo(auctionDetail)
        assertThat(result.getHighestBidAmount()).isEqualTo(2500L)
        assertThat(result.getBidCount()).isEqualTo(3L)

        verify(exactly = 1) { auctionRepository.findAuctionDetailById(auctionId) }
    }

    @Test
    @DisplayName("존재하지 않는 경매 ID로 조회시 예외가 발생한다")
    fun getAuctionDetail_notFound() {
        // given
        val nonExistentAuctionId = 999L
        every { auctionRepository.findAuctionDetailById(nonExistentAuctionId) } returns null

        // when, then
        assertThrows<NotFoundAuctionException> {
            auctionAppService.getAuctionDetail(nonExistentAuctionId)
        }

        verify(exactly = 1) { auctionRepository.findAuctionDetailById(nonExistentAuctionId) }
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 경우 경매 취소에서 예외가 발생한다")
    fun cancelAuction_userNotFound() {
        // given
        val email = "test@example.com"
        val auctionId = 1L
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns null

        // when, then
        assertThrows<NotFoundUserException> {
            auctionAppService.cancelAuction(auctionId)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
        verify(exactly = 0) { auctionService.cancelAuction(any(), any()) }
    }

    @Test
    @DisplayName("경매가 존재하지 않을 경우 경매 취소에서 예외가 발생한다")
    fun cancelAuction_auctionNotFound() {
        // given
        val email = "test@example.com"
        val auctionId = 1L
        val user = mockk<User>()
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { auctionRepository.findByIdOrNull(auctionId) } returns null

        // when, then
        assertThrows<NotFoundAuctionException> {
            auctionAppService.cancelAuction(auctionId)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
        verify(exactly = 1) { auctionRepository.findByIdOrNull(auctionId) }
        verify(exactly = 0) { auctionService.cancelAuction(any(), any()) }
    }

    @Test
    @DisplayName("경매 취소에 성공한다")
    fun cancelAuction_success() {
        // given
        val userId = 1L
        val auctionId = 1L
        val email = "test@example.com"
        val user = mockk<User>()
        val auction = mockk<Auction>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { auctionRepository.findByIdOrNull(auctionId) } returns auction
        every { auctionService.cancelAuction(auction, user) } returns Unit
        every { auctionSchedulerService.unScheduleAuctionJobs(auctionId) } returns Unit

        // when
        auctionAppService.cancelAuction(auctionId)

        // then
        verify { SecurityUtil.getCurrentUsername() }
        verify { userRepository.findByEmail(Email(email)) }
        verify { auctionRepository.findByIdOrNull(auctionId) }
        verify { auctionService.cancelAuction(auction, user) }
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 경우 입찰에서 예외가 발생한다")
    fun placeBid_userNotFound() {
        // given
        val email = "test@example.com"
        val auctionId = 1L
        val amount = 1000L
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns null

        // when, then
        assertThrows<NotFoundUserException> {
            auctionAppService.placeBid(auctionId, amount)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
    }

    @Test
    @DisplayName("경매가 존재하지 않을 경우 입찰에서 예외가 발생한다")
    fun placeBid_auctionNotFound() {
        // given
        val userId = 1L
        val auctionId = 1L
        val amount = 1000L
        val user = mockk<User>()
        val email = "test@example.com"

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { auctionRepository.findByIdOrNull(auctionId) } returns null

        // when, then
        assertThrows<NotFoundAuctionException> {
            auctionAppService.placeBid(auctionId, amount)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
        verify(exactly = 1) { auctionRepository.findByIdOrNull(auctionId) }
    }

    @Test
    @DisplayName("입찰에 성공한다")
    fun placeBid_success() {
        // given
        val auctionId = 1L
        val amount = 1000L
        val email = "test@example.com"
        val user = mockk<User>()
        val auction = mockk<Auction>(relaxed = true)
        val bid = mockk<Bid>(relaxed = true)
        val strategy = mockk<ConcurrencyControlStrategy>()

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { auctionRepository.findByIdOrNull(auctionId) } returns auction
        every { bidRepository.save(any()) } returns bid
        every { strategyRegistry.getCurrentStrategy() } returns strategy
        every { strategy.placeBid(auction, user, Money(amount)) } returns bid

        // when
        val result = auctionAppService.placeBid(auctionId, amount)

        // then
        assertThat(result).isEqualTo(bid.id)
        verify { SecurityUtil.getCurrentUsername() }
        verify { userRepository.findByEmail(Email(email)) }
        verify { auctionRepository.findByIdOrNull(auctionId) }
        verify { bidRepository.save(any()) }
        verify { strategyRegistry.getCurrentStrategy() }
        verify { strategy.placeBid(auction, user, Money(amount)) }
    }

    @Test
    @DisplayName("경매가 존재하지 않을 경우 입찰 리스트 조회에서 예외가 발생한다(getBidsOfAuction)")
    fun getBidsOfAuction_auctionNotFound() {
        // given
        val auctionId = 1L
        val pageNumber = 0

        every { auctionRepository.findByIdOrNull(auctionId) } returns null

        // when, then
        assertThrows<NotFoundAuctionException> {
            auctionAppService.getBidsOfAuction(auctionId, pageNumber)
        }

        verify(exactly = 1) { auctionRepository.findByIdOrNull(auctionId) }
        verify(exactly = 0) { bidRepository.findByAuctionOrderByCreatedAtDesc(any(), any()) }
    }

    @Test
    @DisplayName("입찰 리스트 조회에 성공한다(getBidsOfAuction)")
    fun getBidsOfAuction_success() {
        // given
        val auctionId = 1L
        val pageNumber = 0

        val auction = mockk<Auction>()
        val bid = mockk<Bid>()
        val bidList = PageImpl(listOf(bid))

        every { auctionRepository.findByIdOrNull(auctionId) } returns auction
        every {
            bidRepository.findByAuctionOrderByCreatedAtDesc(auction, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
        } returns bidList

        // when
        val result = auctionAppService.getBidsOfAuction(auctionId, pageNumber)

        // then
        assertThat(result).isEqualTo(bidList)
        verify(exactly = 1) { auctionRepository.findByIdOrNull(auctionId) }
        verify(exactly = 1) {
            bidRepository.findByAuctionOrderByCreatedAtDesc(
                auction,
                PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
            )
        }
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 경우 입찰 리스트 조회에서 예외가 발생한다(getBidsOfUser)")
    fun getBidsOfUser_userNotFound() {
        // given
        val pageNumber = 0
        val email = "test@example.com"
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns null

        // when, then
        assertThrows<NotFoundUserException> {
            auctionAppService.getBidsOfUser(pageNumber)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
        verify(exactly = 0) { bidRepository.findByUserOrderByCreatedAtDesc(any(), any()) }
    }

    @Test
    @DisplayName("입찰 리스트 조회에 성공한다(getBidsOfUser)")
    fun getBidsOfUser_success() {
        // given
        val pageNumber = 0
        val email = "test@example.com"
        val user = mockk<User>()
        val bid = mockk<Bid>()
        val bidList = PageImpl(listOf(bid))

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every {
            bidRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
        } returns bidList

        // when
        val result = auctionAppService.getBidsOfUser(pageNumber)

        // then
        assertThat(result).isEqualTo(bidList)
        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
        verify(exactly = 1) {
            bidRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
        }
    }

    @Test
    @DisplayName("경매 소유자가 존재하지 않을 경우 경매 리스트 조회에서 예외가 발생한다(getAuctionsOfAuctionOwner)")
    fun getAuctionsOfAuctionOwner_userNotFound() {
        // given
        val email = "test@example.com"
        val pageNumber = 0
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns null

        // when, then
        assertThrows<NotFoundUserException> {
            auctionAppService.getAuctionsOfAuctionOwner(pageNumber)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
        verify(exactly = 0) { auctionRepository.findByUser(any(), any()) }
    }

    @Test
    @DisplayName("경매 리스트 조회에 성공한다(getAuctionsOfAuctionOwner)")
    fun getAuctionsOfAuctionOwner_success() {
        // given
        val email = "test@example.com"
        val pageNumber = 0

        val user = mockk<User>()
        val auction = mockk<Auction>()
        val auctionList = PageImpl(listOf(auction))

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every { auctionRepository.findByUser(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE)) } returns auctionList

        // when
        val result = auctionAppService.getAuctionsOfAuctionOwner(pageNumber)

        // then
        assertThat(result).isEqualTo(auctionList)
        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
        verify(
            exactly = 1,
        ) { auctionRepository.findByUser(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE)) }
    }

    @Test
    @DisplayName("입찰자가 존재하지 않을 경우 경매 리스트 조회에서 예외가 발생한다(getAuctionsOfBidder)")
    fun getAuctionsOfBidder_userNotFound() {
        // given
        val email = "test@example.com"
        val pageNumber = 0
        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns null

        // when, then
        assertThrows<NotFoundUserException> {
            auctionAppService.getAuctionsOfBidder(pageNumber)
        }

        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
        verify(exactly = 0) { auctionRepository.findByBidUser(any(), any()) }
    }

    @Test
    @DisplayName("경매 리스트 조회에 성공한다(getAuctionsOfBidder)")
    fun getAuctionsOfBidder_success() {
        // given
        val email = "test@example.com"
        val pageNumber = 0
        val user = mockk<User>()
        val auction = mockk<Auction>()
        val auctionList = PageImpl(listOf(auction))

        every { SecurityUtil.getCurrentUsername() } returns email
        every { userRepository.findByEmail(Email(email)) } returns user
        every {
            auctionRepository.findByBidUser(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
        } returns auctionList

        // when
        val result = auctionAppService.getAuctionsOfBidder(pageNumber)

        // then
        assertThat(result).isEqualTo(auctionList)
        verify(exactly = 1) { SecurityUtil.getCurrentUsername() }
        verify(exactly = 1) { userRepository.findByEmail(Email(email)) }
        verify(
            exactly = 1,
        ) { auctionRepository.findByBidUser(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE)) }
    }
}
