package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.Role
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.vo.Email
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest
    @Autowired
    constructor(
        private val userRepository: UserRepository,
    ) {
        @AfterEach
        fun clean() {
            userRepository.deleteAll()
        }

        @Test
        @DisplayName("이메일로 조회한다")
        fun findByEmailTest() {
            // given
            val email = Email(value = "test@test.com")
            val user = User(email, "Test123456!", "test", Role.CUSTOMER)
            userRepository.save(user)

            // when
            val foundUser = userRepository.findByEmail(email)

            // then
            assertThat(foundUser).isNotNull
            assertThat(foundUser?.email?.value).isEqualTo("test@test.com")
        }
    }
