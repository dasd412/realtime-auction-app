import com.auctionapp.application.dto.request.RegisterProductRequest
import com.auctionapp.application.dto.request.UpdateProductRequest
import com.auctionapp.application.dto.response.*
import com.auctionapp.application.service.ProductAppService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productAppService: ProductAppService,
) {
    @PostMapping
    fun registerProduct(
        @Valid @RequestBody request: RegisterProductRequest,
    ): ResponseEntity<ProductRegisterResponse> {
        val productId =
            productAppService.registerProduct(
                name = request.name,
                description = request.description,
                imageUrl = request.imageUrl,
            )

        return ResponseEntity.ok(ProductRegisterResponse(productId))
    }

    @GetMapping
    fun getProductList(
        @RequestParam(required = false) name: String?,
        @RequestParam(defaultValue = "0") pageNumber: Int,
    ): ResponseEntity<ProductListResponse> {
        return ResponseEntity.ok(productAppService.getProductList(name, pageNumber).toListResponse())
    }

    // 내 상품 목록 조회
    @GetMapping("/my-products")
    fun getMyProducts(): ResponseEntity<List<ProductDetailResponse>> {
        return ResponseEntity.ok(productAppService.getProductListOfUser().map { it.toDetailResponse() })
    }

    @GetMapping("/{productId}")
    fun getProductDetail(
        @PathVariable productId: Long,
    ): ResponseEntity<ProductDetailResponse> {
        return ResponseEntity.ok(productAppService.getProductDetail(productId).toDetailResponse())
    }

    @PutMapping("/{productId}")
    fun updateProduct(
        @PathVariable productId: Long,
        @Valid @RequestBody request: UpdateProductRequest,
    ): ResponseEntity<ProductUpdateResponse> {
        productAppService.updateProduct(
            productId = productId,
            name = request.name,
            description = request.description,
            imageUrl = request.imageUrl,
        )

        return ResponseEntity.ok(ProductUpdateResponse(productId))
    }

    @DeleteMapping("/{productId}")
    fun deleteProduct(
        @PathVariable productId: Long,
    ): ResponseEntity<ProductDeleteResponse> {
        productAppService.deleteProduct(productId)
        return ResponseEntity.ok(ProductDeleteResponse(productId))
    }
}
