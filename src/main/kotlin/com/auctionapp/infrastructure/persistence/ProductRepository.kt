package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% ORDER BY p.id DESC")
    fun findByNameContainingOrderByIdDesc(
        @Param("name") name: String,
        pageable: Pageable,
    ): Page<Product>

    fun findAllByOrderByIdDesc(pageable: Pageable): Page<Product>

    fun findByUserId(userId: Long): List<Product>
}
