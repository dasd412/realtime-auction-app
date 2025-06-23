package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.AuctionStatus
import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AuctionRepository : JpaRepository<Auction, Long> {
    fun findByProduct(product: Product): Auction?

    fun findByStatus(
        status: AuctionStatus,
        pageable: Pageable,
    ): Page<Auction>

    fun findByStatusOrderByStartTimeAsc(
        status: AuctionStatus,
        pageable: Pageable,
    ): Page<Auction>

    // 인기순(입찰 수) 정렬
    @Query("SELECT a FROM Auction a LEFT JOIN a.bids b WHERE a.status = :status GROUP BY a ORDER BY COUNT(b) DESC")
    fun findByStatusOrderByBidsCountDesc(
        @Param("status") status: AuctionStatus,
        pageable: Pageable,
    ): Page<Auction>

    @Query("SELECT a FROM Auction a WHERE a.status = 'NOT_STARTED' AND a.startTime <= :currentTime")
    fun findAuctionsToStart(
        @Param("currentTime") currentTime: LocalDateTime,
    ): List<Auction> // 자동 시작 대상 경매 조회

    @Query("SELECT a FROM Auction a WHERE a.status = 'ACTIVE' AND a.endTime >= :currentTime")
    fun findAuctionsToEnd(
        @Param("currentTime") currentTime: LocalDateTime,
    ): List<Auction> // 자동 종료 대상 경매 조회

    // 사용자가 생성한 경매 목록
    fun findByUser(
        user: User,
        pageable: Pageable,
    ): Page<Auction>

    // 특정 사용자가 입찰한 경매 목록
    @Query("SELECT a FROM Auction a JOIN a.bids b WHERE b.user = :user")
    fun findByBidUser(
        @Param("user") user: User,
        pageable: Pageable,
    ): Page<Auction>
}
