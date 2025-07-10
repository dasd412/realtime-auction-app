package com.auctionapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class RegisterAuctionRequest(
    @field:NotNull(message = "상품 ID는 필수입니다.")
    val productId: Long,
    @field:Min(value = 1, message = "초기 가격은 1원 이상이어야 합니다.")
    val initialPrice: Long,
    @field:Min(value = 1, message = "최소 입찰 단위는 1원 이상이어야 합니다.")
    val minimumBidUnit: Long,
    @field:NotNull(message = "경매 시작 시간은 필수입니다.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val startTime: LocalDateTime,
    @field:NotNull(message = "경매 종료 시간은 필수입니다.")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val endTime: LocalDateTime,
)

data class PlaceBidRequest(
    @field:Min(value = 1, message = "입찰 금액은 1원 이상이어야 합니다.")
    val amount: Long,
)
