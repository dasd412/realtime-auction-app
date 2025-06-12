package com.auctionapp.com.auctionapp.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/", "/index.html", "/static/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/api/auth").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { it.disable() } // 폼 로그인 비활성화
            .httpBasic { it.disable() } //http basic 인증 비활성화

        http.headers { it.frameOptions { it.disable() } }

        return http.build()
    }
}