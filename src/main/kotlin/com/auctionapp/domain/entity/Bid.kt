package com.auctionapp.com.auctionapp.domain.entity

import com.auctionapp.domain.entity.Auction
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class Bid(
    private val amount:Long,
    @ManyToOne
    private val user: User,
    @ManyToOne
    private val auction:Auction,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long?=null,
){

}