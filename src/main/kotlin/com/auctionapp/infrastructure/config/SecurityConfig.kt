package com.auctionapp.com.auctionapp.infrastructure.config

import com.auctionapp.infrastructure.security.JwtAuthenticationFilter
import com.auctionapp.infrastructure.security.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 메서드 수준 인가 설정
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/", "/index.html", "/static/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/auth/login", "/auth/signup").permitAll()
                    .requestMatchers("**/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider, redisTemplate),
                UsernamePasswordAuthenticationFilter::class.java,
            )
            .formLogin { it.disable() } // 폼 로그인 비활성화
            .httpBasic { it.disable() } // http basic 인증 비활성화

        http.headers { it.frameOptions { it.disable() } }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
