package com.auctionapp.com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long>
