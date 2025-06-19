package com.auctionapp.domain.exception

sealed class ProductException(message: String) : RuntimeException(message)

class InvalidProductNameException(message: String = "제품 이름은 비어 있을 수 없습니다") :
    ProductException(message)

class InvalidProductImageUrlException(message: String = "적절한 image url이 아닙니다") :
    ProductException(message)

class AlreadySoldProductException(message: String = "이미 팔린 물건은 등록할 수 없습니다") :
    ProductException(message)

class UnAuthorizedProductException(message: String = "본인의 상품에 대해서만 등록, 수정 및 삭제 가능합니다") :
    ProductException(message)