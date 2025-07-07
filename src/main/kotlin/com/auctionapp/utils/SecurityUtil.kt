package com.auctionapp.com.auctionapp.utils

import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {
    fun getCurrentUsername(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.name // 사용자의 이메일을 반환한다.
    }
}
