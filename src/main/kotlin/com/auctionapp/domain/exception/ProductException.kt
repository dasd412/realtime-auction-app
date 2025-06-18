package com.auctionapp.domain.exception

sealed class ProductException(message: String) : RuntimeException(message)

class InvalidProductNameException(message: String = "제품 이름은 비어 있을 수 없습니다") : ProductException(message)
class InvalidProductImageUrlException(message: String = "적절한 image url이 아닙니다") : ProductException(message)

