package com.auctionapp.application.advice

import com.auctionapp.application.exception.*
import com.auctionapp.com.auctionapp.domain.exception.InvalidAmountException
import com.auctionapp.domain.exception.*
import com.auctionapp.expriment.concurrency.strategy.BidConflictException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.concurrent.TimeoutException

@RestControllerAdvice
@SuppressWarnings("unused")
class ControllerAdvice {
    // 400 Bad Request - 입력값 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ErrorResponse {
        val errors = HashMap<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "유효하지 않은 값입니다"
            errors[fieldName] = errorMessage
        }
        return ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "입력값이 유효하지 않습니다",
            errors = errors,
        )
    }

    // 400 Bad Request - JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "잘못된 요청 형식입니다: ${ex.message}",
        )
    }

    // 400 Bad Request - 비즈니스 규칙 위반
    @ExceptionHandler(
        value = [
            InvalidInitialPriceException::class,
            InvalidMinimumBidUnitException::class,
            InvalidAuctionTimeException::class,
            InvalidAuctionStatusChangeException::class,
            CannotCancelActiveAuctionException::class,
            InvalidBidException::class,
            InvalidAmountException::class,
            InvalidProductNameException::class,
            InvalidProductImageUrlException::class,
            InvalidEmailException::class,
            InvalidPasswordException::class,
            InvalidUserNameException::class,
            UnavailableMethodInAuctionException::class,
            AlreadySoldProductException::class,
        ],
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBusinessRuleViolation(ex: Exception): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "유효하지 않은 요청입니다",
        )
    }

    // 401 Unauthorized - 인증 실패
    @ExceptionHandler(
        value = [
            UnauthorizedException::class,
            LoginFailException::class,
            UnavailableRefreshTokenException::class,
            LogoutFailException::class,
        ],
    )
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorized(ex: Exception): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            message = ex.message ?: "인증에 실패했습니다",
        )
    }

    // 403 Forbidden - 권한 부족
    @ExceptionHandler(
        value = [
            UnAuthorizedCancelAuctionException::class,
            UnAuthorizedProductException::class,
            NotProductOwnerException::class,
        ],
    )
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbidden(ex: Exception): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = ex.message ?: "접근 권한이 없습니다",
        )
    }

    // 404 Not Found - 리소스 없음
    @ExceptionHandler(
        value = [
            NotFoundAuctionException::class,
            NotFoundProductException::class,
            NotFoundUserException::class,
        ],
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: Exception): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "요청한 리소스를 찾을 수 없습니다",
        )
    }

    // 409 Conflict - 리소스 충돌
    @ExceptionHandler(
        value = [
            DuplicateEmailException::class,
            BidConflictException::class,
        ],
    )
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflict(ex: Exception): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "요청이 현재 서버 상태와 충돌합니다",
        )
    }

    // 429 Too Many Requests - 과도한 요청
    @ExceptionHandler(
        value = [
            TimeoutException::class,
        ],
    )
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun handleTooManyRequests(ex: Exception): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.TOO_MANY_REQUESTS.value(),
            message = ex.message ?: "너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요.",
        )
    }

    // 500 Internal Server Error - 서버 내부 오류
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllUncaughtException(ex: Exception): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "서버 내부 오류가 발생했습니다: ${ex.message}",
        )
    }
}

// 에러 응답 DTO
data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: Map<String, String> = emptyMap(),
)
