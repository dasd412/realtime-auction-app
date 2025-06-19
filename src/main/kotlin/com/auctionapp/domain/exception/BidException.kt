package com.auctionapp.domain.exception

sealed class BidException(message: String) : RuntimeException(message)

class InvalidBidException(message: String = "유효하지 않은 입찰입니다") : BidException(message)
