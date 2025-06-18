package com.auctionapp.domain.entity

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AuctionTest {
    @Test
    @DisplayName("초기 가격이 1000원 미만이면 실패한다")
    fun auctionInvalidInitialPriceTest() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("최소 입찰 단위가 음수면 실패한다")
    fun auctionInvalidMinimumBidUnitPriceTest() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("종료 시각이 시작 시간보다 1시간 이후가 아니면 실패한다")
    fun auctionInvalidTimeSequenceTest() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("적절한 초기 가격, 최소 입찰 단위, 시작 시각, 종료 시각이면 성공한다")
    fun auctionTest() {
        //given

        //when

        //then
    }
}