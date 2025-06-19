package com.auctionapp.domain.exception

sealed class UserException(message: String) : RuntimeException(message)

class InvalidEmailException(message: String = "적절한 이메일 형식이 아닙니다") : UserException(message)
class InvalidPasswordException(message: String = "적절한 비밀번호 형식이 아닙니다") : UserException(message)
class InvalidUserNameException(message: String = "이름은 비어 있을 수 없습니다") : UserException(message)