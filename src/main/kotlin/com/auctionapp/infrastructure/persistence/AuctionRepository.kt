package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuctionRepository : JpaRepository<Auction, Long>{
    fun findByProduct(product: Product):Auction?
}
