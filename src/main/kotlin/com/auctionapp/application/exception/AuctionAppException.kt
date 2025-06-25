package com.auctionapp.application.exception

sealed class AuctionAppException(message: String) : RuntimeException(message)

class NotFoundAuctionException(message: String = "존재 하지 않는 경매입니다") : AuctionAppException(message)
