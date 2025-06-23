package com.auctionapp.infrastructure.persistence

import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.Role
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.vo.Email
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProductRepositoryTest
    @Autowired
    constructor(
        private val userRepository: UserRepository,
        private val productRepository: ProductRepository,
    ) {
        @AfterEach
        fun clean() {
            productRepository.deleteAll()
            userRepository.deleteAll()
        }

        @Test
        fun findByNameContainingOrderByIdDescTest() {
            // given
            val email = Email(value = "test@test.com")
            val user = User(email, "Test123456!", "test", Role.CUSTOMER)
            userRepository.save(user)

            // 검색어로 사용할 문자열이 포함된 상품명 생성
            val searchKeyword = "apple"

            // 20개의 "apple" 포함 상품 생성
            for (i in 1..20) {
                val product =
                    Product(
                        name = "apple product $i",
                        imageUrl = "https://test.com/apple_$i.jpg",
                        user = user,
                    )
                productRepository.save(product)
            }

            // 18개의 "orange" 포함 상품 생성
            for (i in 1..18) {
                val product =
                    Product(
                        name = "orange product $i",
                        imageUrl = "https://test.com/orange_$i.jpg",
                        user = user,
                    )
                productRepository.save(product)
            }

            // when
            val pageable = PageRequest.of(0, 20)
            val result = productRepository.findByNameContainingOrderByIdDesc(searchKeyword, pageable)

            // then
            // 총 항목 수 검증
            assertThat(result.totalElements).isEqualTo(20)

            // 페이지 크기 검증
            assertThat(result.content.size).isEqualTo(20)

            // 모든 결과가 검색어를 포함하는지 검증
            assertThat(result.content).allMatch { it.name.contains(searchKeyword) }

            // 최신순 정렬 검증 (가장 마지막에 저장된 상품이 첫 번째로 조회)
            assertThat(result.content[0].name).isEqualTo("apple product 20")
            assertThat(result.content[19].name).isEqualTo("apple product 1")
        }

        @Test
        @DisplayName("상품을 최신순으로 정렬해서 조회한다")
        fun findAllByOrderByIdDescTest() {
            // given
            val email = Email(value = "test@test.com")
            val user = User(email, "Test123456!", "test", Role.CUSTOMER)
            userRepository.save(user)

            // 17개의 상품 생성 및 저장
            val productNames = (1..17).map { "상품 $it" }
            productNames.map { name ->
                val product =
                    Product(
                        name = name,
                        imageUrl = "https://test.com/${name.replace(" ", "_")}.jpg",
                        user = user,
                    )
                productRepository.save(product)
            }

            // when
            // 요구사항에 맞게 기본 페이지 크기 20으로 설정
            val pageable = PageRequest.of(0, 20)
            val result = productRepository.findAllByOrderByIdDesc(pageable)

            // then
            // 총 항목 수 검증
            assertThat(result.totalElements).isEqualTo(17)

            // 페이지 크기 검증
            assertThat(result.content.size).isEqualTo(17)

            // 최신순 정렬 검증 (ID 내림차순)
            // 가장 마지막에 저장된 상품이 첫 번째로 조회되어야 함
            assertThat(result.content[0].name).isEqualTo("상품 17")
            assertThat(result.content[16].name).isEqualTo("상품 1")

            // 전체 순서 검증
            val expectedOrder = (17 downTo 1).map { "상품 $it" }
            assertThat(result.content)
                .extracting("name")
                .containsExactlyElementsOf(expectedOrder)
        }

        @Test
        @DisplayName("사용자 id로 상품을 조회한다")
        fun findByUserIdTest() {
            // given
            val email = Email(value = "test@test.com")
            val user = User(email, "Test123456!", "test", Role.CUSTOMER)
            val savedUser = userRepository.save(user)

            val product1 = Product(name = "test1", imageUrl = "https://test.com/test1.jpg", user = user)
            productRepository.save(product1)

            val product2 = Product(name = "test2", imageUrl = "https://test.com/test2.jpg", user = user)
            productRepository.save(product2)

            // when
            val results = productRepository.findByUserId(savedUser.id!!)

            // then
            assertThat(results).hasSize(2)
            assertThat(results).extracting("name").containsExactlyInAnyOrder("test1", "test2")
            assertThat(results).extracting("imageUrl")
                .containsExactlyInAnyOrder("https://test.com/test1.jpg", "https://test.com/test2.jpg")
        }
    }
