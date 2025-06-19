package com.auctionapp.domain.entity

import com.auctionapp.com.auctionapp.domain.exception.InvalidAmountException
import com.auctionapp.domain.vo.Money
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MoneyTest {
    @Test
    @DisplayName("금액이 음수면 실패한다")
    fun minusAmountTest() {
        assertThrows<InvalidAmountException> {
            Money(-1000L)
        }
    }

    @Test
    @DisplayName("금액 더하기")
    fun addTest() {
        val money1 = Money(1000L)
        val money2 = Money(2000L)
        val result = money1.add(money2)
        assertThat(result.amount).isEqualTo(3000L)
    }

    @Test
    @DisplayName("금액 빼기")
    fun subtractTest() {    
        val money1 = Money(2000L)
        val money2 = Money(1000L)
        val result = money1.subtract(money2)
        assertThat(result.amount).isEqualTo(1000L)
    }

    @Test
    @DisplayName("금액을 뺏더니 음수면 실패한다")
    fun subtractTest2() {
        val money1 = Money(1000L)
        val money2 = Money(2000L)
        assertThrows<InvalidAmountException> {
            money1.subtract(money2)
        }
    }

    @Test
    @DisplayName("금액 비교1")
    fun compareToTest1() {
        val money1 = Money(2000L)
        val money2 = Money(1000L)
        val result = money1.compareTo(money2)
        assertThat(result).isEqualTo(1)
    }

    @Test
    @DisplayName("금액 비교2")
    fun compareToTest2() {
        val money1 = Money(1000L)
        val money2 = Money(2000L)
        val result = money1.compareTo(money2)
        assertThat(result).isEqualTo(-1)
    }

    @Test
    @DisplayName("금액 비교3")
    fun compareToTest3() {
        val money1 = Money(1000L)
        val money2 = Money(1000L)
        val result = money1.compareTo(money2)
        assertThat(result).isEqualTo(0)
    }
}