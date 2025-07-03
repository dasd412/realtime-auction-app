package com.auctionapp.expriment.concurrency.strategy

class BidConflictException(message: String = "다른 입찰이 처리 중입니다. 잠시 후 다시 시도해주세요.") : RuntimeException(message)
