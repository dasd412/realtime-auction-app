package com.auctionapp.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Auction(
    private var initialPrice: Long,
    private var minimumBidUnit:Long,
    private var startTime:LocalDateTime,
    private var endTime:LocalDateTime,
    @Enumerated(EnumType.STRING)
    private var status:AuctionStatus,
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
        if(endTime.isBefore(startTime.plusHours(1))){
            throw IllegalArgumentException("종료 시각은 시작 시간보다 최소 1시간 이후여야 합니다")
        }
    }
}