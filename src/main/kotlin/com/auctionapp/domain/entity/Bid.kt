package com.auctionapp.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class Bid(
    private val amount:Long,
    private val createdAt:LocalDateTime,
    @ManyToOne
    private val user: User,
    @ManyToOne
    private val auction:Auction,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long?=null,
){
    init{
        if(amount<=0){
            throw IllegalArgumentException("입찰 금액은 음수가 될 수 없습니다")
        }
        // 최고 입찰가와 최소 입찰 단위 검증은 서비스 레이어에서 수행
        // 왜냐하면 현재 최고 입찰가를 알기 위해서는 auction의 모든 bids를 조회해야 하기 때문
    }
}