package com.auctionapp.application.exception

sealed class ProductAppException(message: String) : RuntimeException(message)

class NotFoundProductException(message: String = "존재 하지 않는 상품입니다") : ProductAppException(message)

class NotProductOwnerException(message: String = "본인의 상품이 아닙니다") : ProductAppException(message)

class UnavailableMethodInAuctionException(message: String = "경매 중에 지원되지 않는 메서드입니다") : ProductAppException(message)
