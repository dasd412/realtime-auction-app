package com.auctionapp.expriment.concurrency.strategy

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.vo.Money

interface ConcurrencyControlStrategy {
    fun placeBid(
        auction: Auction,
        user: User,
        amount: Money,
    ): Bid
}
