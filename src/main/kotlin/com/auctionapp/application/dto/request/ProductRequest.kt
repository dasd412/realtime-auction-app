package com.auctionapp.application.dto.request

import jakarta.validation.constraints.NotBlank

data class RegisterProductRequest(
    @field:NotBlank(message = "상품명은 필수입니다.")
    val name: String,
    val description: String?,
    @field:NotBlank(message = "이미지 URL은 필수입니다.")
    val imageUrl: String,
)

data class UpdateProductRequest(
    @field:NotBlank(message = "상품명은 필수입니다.")
    val name: String,
    val description: String?,
    @field:NotBlank(message = "이미지 URL은 필수입니다.")
    val imageUrl: String,
)
