package com.auctionapp.application.controller

import com.auctionapp.TestSecurityConfig
import com.auctionapp.application.dto.request.RegisterProductRequest
import com.auctionapp.application.dto.request.UpdateProductRequest
import com.auctionapp.application.exception.NotFoundProductException
import com.auctionapp.application.exception.NotProductOwnerException
import com.auctionapp.application.exception.UnavailableMethodInAuctionException
import com.auctionapp.application.service.ProductAppService
import com.auctionapp.domain.entity.Product
import com.auctionapp.domain.entity.ProductStatus
import com.auctionapp.domain.entity.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Import(TestSecurityConfig::class)
@WebMvcTest(controllers = [ProductController::class])
class ProductControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var productAppService: ProductAppService

    @Test
    @DisplayName("상품 등록 성공 테스트")
    fun registerProductSuccess() {
        // given
        val request =
            RegisterProductRequest(
                name = "테스트 상품",
                description = "테스트 상품 설명",
                imageUrl = "https://example.com/image.jpg",
            )
        val productId = 1L

        given(
            productAppService.registerProduct(
                name = request.name,
                description = request.description,
                imageUrl = request.imageUrl,
            ),
        ).willReturn(productId)

        // when & then
        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.productId").value(productId))
    }

    @Test
    @DisplayName("상품 목록 조회 테스트")
    fun getProductListSuccess() {
        // given
        val product1 = createSampleProduct(1L, "상품1")
        val product2 = createSampleProduct(2L, "상품2")
        val products = listOf(product1, product2)
        val page = PageImpl(products, PageRequest.of(0, 20), 2)

        given(productAppService.getProductList(null, 0)).willReturn(page)

        // when & then
        mockMvc.perform(
            get("/api/products")
                .param("pageNumber", "0"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.products.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    @DisplayName("이름으로 상품 검색 테스트")
    fun getProductListByNameSuccess() {
        // given
        val product = createSampleProduct(1L, "검색상품")
        val page = PageImpl(listOf(product), PageRequest.of(0, 20), 1)

        given(productAppService.getProductList("검색", 0)).willReturn(page)

        // when & then
        mockMvc.perform(
            get("/api/products")
                .param("name", "검색")
                .param("pageNumber", "0"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.products.length()").value(1))
            .andExpect(jsonPath("$.products[0].name").value("검색상품"))
    }

    @Test
    @DisplayName("내 상품 목록 조회 테스트")
    fun getMyProductsSuccess() {
        // given
        val product1 = createSampleProduct(1L, "내 상품1")
        val product2 = createSampleProduct(2L, "내 상품2")
        val products = listOf(product1, product2)

        given(productAppService.getProductListOfUser()).willReturn(products)

        // when & then
        mockMvc.perform(
            get("/api/products/my-products"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    @DisplayName("상품 상세 조회 성공 테스트")
    fun getProductDetailSuccess() {
        // given
        val productId = 1L
        val product = createSampleProduct(productId, "상세 조회 상품")

        given(productAppService.getProductDetail(productId)).willReturn(product)

        // when & then
        mockMvc.perform(
            get("/api/products/{productId}", productId),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.productId").value(productId))
            .andExpect(jsonPath("$.name").value("상세 조회 상품"))
    }

    @Test
    @DisplayName("존재하지 않는 상품 상세 조회 실패 테스트")
    fun getProductDetailNotFound() {
        // given
        val productId = 999L

        given(productAppService.getProductDetail(productId)).willThrow(NotFoundProductException())

        // when & then
        mockMvc.perform(
            get("/api/products/{productId}", productId),
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    @DisplayName("상품 수정 성공 테스트")
    fun updateProductSuccess() {
        // given
        val productId = 1L
        val request =
            UpdateProductRequest(
                name = "수정된 상품",
                description = "수정된 설명",
                imageUrl = "https://example.com/updated-image.jpg",
            )

        doNothing().`when`(productAppService).updateProduct(
            productId = productId,
            name = request.name,
            description = request.description,
            imageUrl = request.imageUrl,
        )

        // when & then
        mockMvc.perform(
            put("/api/products/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.productId").value(productId))
    }

    @Test
    @DisplayName("자신의 상품이 아닌 경우 수정 실패 테스트")
    fun updateProductNotOwner() {
        // given
        val productId = 2L
        val request =
            UpdateProductRequest(
                name = "수정 실패 상품",
                description = "수정 실패 설명",
                imageUrl = "https://example.com/image.jpg",
            )

        doThrow(NotProductOwnerException()).`when`(productAppService).updateProduct(
            productId = productId,
            name = request.name,
            description = request.description,
            imageUrl = request.imageUrl,
        )

        // when & then
        mockMvc.perform(
            put("/api/products/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
    }

    @Test
    @DisplayName("경매 중인 상품 수정 실패 테스트")
    fun updateProductInAuction() {
        // given
        val productId = 3L
        val request =
            UpdateProductRequest(
                name = "경매 중 상품",
                description = "경매 중 설명",
                imageUrl = "https://example.com/image.jpg",
            )

        doThrow(UnavailableMethodInAuctionException()).`when`(productAppService).updateProduct(
            productId = productId,
            name = request.name,
            description = request.description,
            imageUrl = request.imageUrl,
        )

        // when & then
        mockMvc.perform(
            put("/api/products/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    @DisplayName("상품 삭제 성공 테스트")
    fun deleteProductSuccess() {
        // given
        val productId = 1L

        doNothing().`when`(productAppService).deleteProduct(productId)

        // when & then
        mockMvc.perform(
            delete("/api/products/{productId}", productId),
        )
            .andDo(print())
            .andExpect(status().isNoContent())
    }

    @Test
    @DisplayName("자신의 상품이 아닌 경우 삭제 실패 테스트")
    fun deleteProductNotOwner() {
        // given
        val productId = 2L

        doThrow(NotProductOwnerException()).`when`(productAppService).deleteProduct(productId)

        // when & then
        mockMvc.perform(
            delete("/api/products/{productId}", productId),
        )
            .andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
    }

    @Test
    @DisplayName("경매 중인 상품 삭제 실패 테스트")
    fun deleteProductInAuction() {
        // given
        val productId = 3L

        doThrow(UnavailableMethodInAuctionException()).`when`(productAppService).deleteProduct(productId)

        // when & then
        mockMvc.perform(
            delete("/api/products/{productId}", productId),
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    // 테스트용 샘플 Product 객체 생성 헬퍼 메서드
    private fun createSampleProduct(
        id: Long,
        name: String,
    ): Product {
        // User 객체를 직접 생성하지 않고 목킹
        val user = mock(User::class.java)
        `when`(user.id).thenReturn(1L)
        `when`(user.name).thenReturn("사용자")

        // Product 객체를 직접 생성하지 않고 목킹
        val product = mock(Product::class.java)
        `when`(product.id).thenReturn(id)
        `when`(product.name).thenReturn(name)
        `when`(product.description).thenReturn("설명")
        `when`(product.imageUrl).thenReturn("https://example.com/image.jpg")
        `when`(product.status).thenReturn(ProductStatus.AVAILABLE)
        `when`(product.user).thenReturn(user)

        return product
    }
}
