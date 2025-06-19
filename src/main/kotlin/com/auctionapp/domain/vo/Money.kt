package com.auctionapp.domain.vo

import jakarta.persistence.Embeddable

@Embeddable
data class Money(val amount: Long) : Comparable<Money> {
    init {
        require(amount >= 0) {
            "금액은 0 이상이어야 합니다."
        }
    }

    fun add(other: Money): Money = Money(this.amount + amount)
    fun subtract(other: Money): Money = Money(this.amount - amount)
    fun isGreaterThan(other: Money): Boolean = this.amount > other.amount
    fun isGreaterThanOrEqual(other: Money): Boolean = this.amount >= other.amount
    
    override fun compareTo(other: Money): Int {
        return if (this.amount > other.amount) {
            1
        } else if (this.amount < other.amount) {
            -1
        } else {
            0
        }
    }
}