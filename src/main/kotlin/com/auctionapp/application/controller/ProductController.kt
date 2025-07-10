import com.auctionapp.application.dto.request.RegisterProductRequest
import com.auctionapp.application.dto.request.UpdateProductRequest
import com.auctionapp.application.service.ProductAppService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productAppService: ProductAppService
) {

    @PostMapping
    fun registerProduct(@Valid @RequestBody request: RegisterProductRequest): ResponseEntity<Map<String, Long>> {
        val productId = productAppService.registerProduct(
            name = request.name,
            description = request.description,
            imageUrl = request.imageUrl
        )

        return ResponseEntity.ok(mapOf("productId" to productId))
    }

    // 상품 목록 조회
    @GetMapping
    fun getProductList(
        @RequestParam(required = false) name: String?,
        @RequestParam(defaultValue = "0") pageNumber: Int
    ) = productAppService.getProductList(name, pageNumber)

    // 내 상품 목록 조회
    @GetMapping("/my-products")
    fun getMyProducts() = productAppService.getProductListOfUser()

    @GetMapping("/{productId}")
    fun getProductDetail(@PathVariable productId: Long) = productAppService.getProductDetail(productId)

    @PutMapping("/{productId}")
    fun updateProduct(
        @PathVariable productId: Long,
        @Valid @RequestBody request: UpdateProductRequest
    ): ResponseEntity<Map<String, String>> {
        productAppService.updateProduct(
            productId = productId,
            name = request.name,
            description = request.description,
            imageUrl = request.imageUrl
        )

        return ResponseEntity.ok(mapOf("message" to "상품이 수정되었습니다."))
    }

    @DeleteMapping("/{productId}")
    fun deleteProduct(@PathVariable productId: Long): ResponseEntity<Map<String, String>> {
        productAppService.deleteProduct(productId)
        return ResponseEntity.ok(mapOf("message" to "상품이 삭제되었습니다."))
    }
}
