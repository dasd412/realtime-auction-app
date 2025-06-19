package com.auctionapp.domain.vo

import com.auctionapp.domain.exception.InvalidEmailException
import jakarta.persistence.Embeddable

@Embeddable
data class Email(val value: String) {
    init {
        val pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        if (!Regex(pattern).matches(value)) {
            throw InvalidEmailException()
        }
    }
}