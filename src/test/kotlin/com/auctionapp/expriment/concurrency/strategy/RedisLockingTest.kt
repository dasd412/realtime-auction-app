package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.TestRedisConfig
import com.auctionapp.application.service.AuctionAppService
import com.auctionapp.domain.entity.*
import com.auctionapp.domain.vo.Money
import com.auctionapp.infrastructure.persistence.AuctionRepository
import com.auctionapp.infrastructure.persistence.BidRepository
import com.auctionapp.infrastructure.persistence.ProductRepository
import com.auctionapp.infrastructure.persistence.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfig::class)
class RedisLockingTest(
    @Autowired private val auctionAppService: AuctionAppService,
    @Autowired private val auctionRepository: AuctionRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val bidRepository: BidRepository,
) {
    @Test
    @DisplayName("Redis 분산 락을 통해 입찰이 성공적으로 처리된다")
    fun shouldPlaceBidWithRedisLock() {
        // given
        val user = userRepository.save(User.fixture())
        val product = productRepository.save(Product.fixture(user = user, status = ProductStatus.AVAILABLE))
        val auction =
            auctionRepository.save(
                Auction.fixture(
                    initialPrice = Money(1000),
                    product = product,
                    user = user,
                    status = AuctionStatus.ACTIVE,
                ),
            )
        val bidAmount = 2000L
        val bidder = userRepository.save(User.fixture())

        // when
        val bidId = auctionAppService.placeBidWithRedisLock(bidder.id!!, auction.id!!, bidAmount)

        // then
        val savedBid = bidRepository.findById(bidId).orElse(null)
        assertThat(savedBid).isNotNull
        assertThat(savedBid.amount).isEqualTo(Money(bidAmount))
        assertThat(savedBid.user.id).isEqualTo(bidder.id)
    }
}
