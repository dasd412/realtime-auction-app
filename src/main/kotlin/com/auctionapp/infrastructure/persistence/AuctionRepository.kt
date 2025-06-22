package com.auctionapp.com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.Auction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuctionRepository : JpaRepository<Auction, Long>
