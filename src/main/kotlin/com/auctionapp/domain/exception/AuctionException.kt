package com.auctionapp.domain.exception

sealed class AuctionException(message: String) : RuntimeException(message)

class InvalidInitialPriceException(message: String = "초기 가격은 1000원 이상이어야 합니다") : AuctionException(message)
class InvalidMinimumBidUnitException(message: String = "최소 입찰 단위는 음수가 될 수 없습니다") : AuctionException(message)
class InvalidAuctionTimeException(message: String = "종료 시각은 시작 시간보다 최소 1시간 이후여야 합니다") : AuctionException(message)
class CannotCancelActiveAuctionException(message: String = "경매가 시작되면, 취소 상태로 변경할 수 없습니다") : AuctionException(message)
class UnAuthorizedCancelAuctionException(message: String = "권한이 없는 사용자는 경매를 취소할 수 없습니다"): AuctionException(message)
