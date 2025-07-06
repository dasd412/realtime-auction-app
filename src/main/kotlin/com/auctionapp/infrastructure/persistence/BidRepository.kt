package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BidRepository : JpaRepository<Bid, Long> {
    @Query("SELECT b FROM Bid b WHERE b.auction = :auction ORDER BY b.createdAt DESC")
    fun findByAuctionOrderByCreatedAtDesc(
        @Param("auction")auction: Auction,
        pageable: Pageable,
    ): Page<Bid>

    @Query("SELECT b FROM Bid b WHERE b.user = :user ORDER BY b.createdAt DESC")
    fun findByUserOrderByCreatedAtDesc(
        @Param("user") user: User,
        pageable: Pageable,
    ): Page<Bid>

    fun findTopByAuctionOrderByAmountDesc(auction: Auction): Bid
}
