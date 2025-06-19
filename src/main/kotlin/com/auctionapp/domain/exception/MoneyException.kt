package com.auctionapp.com.auctionapp.domain.exception

sealed class MoneyException(message: String) : RuntimeException(message)

class InvalidAmountException(message: String = "금액은 음수가 될 수 없습니다") : MoneyException(message)
