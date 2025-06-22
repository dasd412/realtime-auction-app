package com.auctionapp.domain.service

import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.exception.AlreadySoldProductException
import org.springframework.stereotype.Service

@Service
class ProductService {
    fun registerProduct(
        product: Product,
        user: User,
    ) {
        if (product.isSold()) {
            throw AlreadySoldProductException()
        }
        product.markAsAvailable()
        user.registerProduct(product)
    }
}
