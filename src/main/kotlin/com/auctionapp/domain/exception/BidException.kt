package com.auctionapp.domain.exception

sealed class BidException(message: String) : RuntimeException(message)

class InvalidBidException(message: String = "유효하지 않은 입찰입니다") : BidException(message)
class InvalidBidAmountException(message: String = "입찰 금액은 0보다 크고 최고 입찰 금액보다 클 수 없습니다") : BidException(message)

