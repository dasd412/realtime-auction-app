package com.auctionapp.domain.service

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.exception.AlreadySoldProductException
import com.auctionapp.domain.exception.UnAuthorizedCancelAuctionException
import com.auctionapp.domain.exception.UnAuthorizedProductException
import com.auctionapp.domain.vo.Money
import org.springframework.stereotype.Service

@Service
class AuctionService {
    fun registerAuction(
        auction: Auction,
        user: User,
        product: Product,
    ) {
        if (!user.isOwnerOf(product)) {
            throw UnAuthorizedProductException()
        }
        if (product.isSold()) {
            throw AlreadySoldProductException()
        }

        user.registerAuction(auction)
    }

    fun startAuction(auction: Auction) {
        auction.start()
    }

    fun endAuction(auction: Auction) {
        auction.end()
    }

    fun cancelAuction(
        auction: Auction,
        user: User,
    ) {
        if (!user.isOwnerOf(auction)) {
            throw UnAuthorizedCancelAuctionException()
        }
        auction.cancel()
    }

    fun placeBid(
        amount: Money,
        user: User,
        auction: Auction,
    ): Bid {
        val bid = Bid.create(amount, user, auction)
        user.placeBid(bid)
        auction.addBid(bid)
        return bid
    }
}
