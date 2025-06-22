package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.User
import com.auctionapp.domain.vo.Email
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: Email): Boolean

    fun findByEmailAndPassword(email: Email, password: String): User?
}
