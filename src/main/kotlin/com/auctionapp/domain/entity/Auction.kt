package com.auctionapp.domain.entity

import com.auctionapp.com.auctionapp.domain.entity.AuctionStatus
import com.auctionapp.com.auctionapp.domain.entity.Bid
import com.auctionapp.com.auctionapp.domain.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Auction(
    private var initialPrice: Long,
    private var minimumBidUnit:Long,
    private var startTime:LocalDateTime,
    private var endTime:LocalDateTime,
    @Enumerated(EnumType.STRING)
    private var staus:AuctionStatus,
    @ManyToOne
    private val user: User,
    @OneToOne
    private val product: Product,
    @OneToMany(mappedBy = "auction", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val bids: MutableList<Bid> = mutableListOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long?=null,
){
    init {
        if (initialPrice < 1000) {
            throw IllegalArgumentException("초기 가격은 1000원 이상이어야 합니다")
        }
    }
}