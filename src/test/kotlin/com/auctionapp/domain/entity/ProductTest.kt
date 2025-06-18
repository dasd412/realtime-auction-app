package com.auctionapp.domain.entity

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ProductTest {
    @Test
    @DisplayName("상품 몀 길이가 3자 미만이면 실패한다")
    fun productInvalidNameLengthTest1() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("상품 몀 길이가 100자 초과면 실패한다")
    fun productInvalidNameLengthTest2() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("상품 몀 길이가 3자 이상 100자 이하면 성공한다")
    fun productValidNameLengthTest() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("부적절한 이미지 url이면 실패한다")
    fun productInvalidImageUrlTest() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("적절한 이미지 url이면 성공한다")
    fun productValidImageUrlTest() {
        //given

        //when

        //then
    }
}