package com.auctionapp.com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.Bid
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BidRepository : JpaRepository<Bid, Long> {
}