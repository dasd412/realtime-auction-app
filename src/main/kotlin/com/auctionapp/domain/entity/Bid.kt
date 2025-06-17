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

}