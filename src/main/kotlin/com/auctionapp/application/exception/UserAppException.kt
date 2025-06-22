package com.auctionapp.application.exception

sealed class UserAppException(message: String) : RuntimeException(message)

class DuplicateEmailException(message: String = "이미 존재하는 이메일입니다") : UserAppException(message)
class LoginFailException(message: String = "로그인에 실패했습니다") : UserAppException(message)
class NotFoundUserException(message: String = "존재 하지 않는 사용자입니다") : UserAppException(message)
