package com.auctionapp

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@TestConfiguration
@SuppressWarnings("unused")
class TestSecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() } // CSRF 보호 비활성화
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll() // 모든 요청 허용
            }
            .build()
    }
}
