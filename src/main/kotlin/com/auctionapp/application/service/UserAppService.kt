package com.auctionapp.com.auctionapp.application.service


import com.auctionapp.application.exception.DuplicateEmailException
import com.auctionapp.application.exception.LoginFailException
import com.auctionapp.domain.entity.Role
import com.auctionapp.domain.entity.User
import com.auctionapp.domain.vo.Email
import com.auctionapp.infrastructure.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserAppService(
    private val userRepository: UserRepository,
) {
    //todo 기억이 잘 안나지만, spring security 이용해야 할 듯. passoword는 암호화해야 함.
    @Transactional
    fun signUp(
        email: Email,
        password: String,
        name: String,
    ): User {
        val exist = userRepository.existsByEmail(email)

        if (exist) {
            throw DuplicateEmailException()
        }

        val user = User(email = email, password = password, name = name, role = Role.CUSTOMER)

        return userRepository.save(user)
    }

    @Transactional
    fun signIn(
        email: Email,
        password: String,
    ) :String{
        val found=userRepository.findByEmailAndPassword(email,password)?:throw LoginFailException()
        //todo 스프링 시큐리티 활용 및 jwt 토큰 반환 로직 추가
        return ""
    }

    fun refreshToken(accessToken: String): String {
        //todo 스프링 시큐리티 활용 및 jwt 토큰 반환 로직 추가
        return ""
    }
}
