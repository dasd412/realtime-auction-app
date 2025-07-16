package com.auctionapp.infrastructure.event.exception

class UnsupportedEventException(message: String = "지원하지 않는 도메인 이벤트입니다") : IllegalArgumentException(message)
