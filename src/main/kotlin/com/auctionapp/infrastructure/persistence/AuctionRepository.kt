package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.AuctionStatus
import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.User
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

interface AuctionDetail {
    fun getAuction(): Auction

    fun getBidCount(): Long

    fun getHighestBidAmount(): Long?
}

@Repository
interface AuctionRepository : JpaRepository<Auction, Long> {
    fun findByProduct(product: Product): Auction?

    @Query(
        "SELECT a AS auction, COUNT(b) AS bidCount, MAX(b.amount.amount) AS highestBidAmount " +
            "FROM Auction a LEFT JOIN a.bids b WHERE a.id = :auctionId GROUP BY a",
    )
    fun findAuctionDetailById(
        @Param("auctionId") auctionId: Long,
    ): AuctionDetail?

    fun findByStatus(
        status: AuctionStatus,
        pageable: Pageable,
    ): Page<Auction>

    fun findByStatusOrderByStartTimeAsc(
        status: AuctionStatus,
        pageable: Pageable,
    ): Page<Auction>

    // 인기순(입찰 수) 정렬 (paging이므로 fetch 안함.)
    @Query("SELECT a FROM Auction a LEFT JOIN a.bids b WHERE a.status = :status GROUP BY a ORDER BY COUNT(b) DESC")
    fun findByStatusOrderByBidsCountDesc(
        @Param("status") status: AuctionStatus,
        pageable: Pageable,
    ): Page<Auction>

    // 사용자가 생성한 경매 목록  (paging이므로 fetch 안함.)
    fun findByUser(
        user: User,
        pageable: Pageable,
    ): Page<Auction>

    // 특정 사용자가 입찰한 경매 목록  (paging이므로 fetch 안함.)
    @Query("SELECT a FROM Auction a JOIN a.bids b WHERE b.user = :user")
    fun findByBidUser(
        @Param("user") user: User,
        pageable: Pageable,
    ): Page<Auction>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    fun findByIdWithPessimisticLock(
        @Param("id")id: Long,
    ): Auction?

    @Query("SELECT a FROM Auction a JOIN FETCH a.user u JOIN FETCH a.product p WHERE a.id = :id")
    fun findByIdWithUserAndProduct(
        @Param("id")id: Long,
    ): Auction?
}
