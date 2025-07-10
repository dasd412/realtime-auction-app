package com.auctionapp.application.dto.request
import com.auctionapp.domain.entity.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignupRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\\$%^&*()_+=!~]).{8,16}$",
        message = "비밀번호는 숫자, 소문자, 대문자, 특수문자를 각각 1개 이상 포함하고 8~16자여야 합니다.",
    )
    val password: String,
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
    val role: Role = Role.CUSTOMER,
)

data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
)
