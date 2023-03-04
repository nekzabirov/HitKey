package com.hitkey.system.exception

import com.hitkey.common.data.HitResponse
import com.hitkey.system.database.repo.UserPhoneRepo
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

class TokenExpiredException: RuntimeException()

class WrongPhoneNumberFormat: RuntimeException()

class FaultPhoneConfirm: RuntimeException()

class PhoneAlreadyConfirmed: RuntimeException()

class FaultLogin: RuntimeException()

class EmailAlreadyConfirmed: RuntimeException()

class UserNotFound: RuntimeException()

@RestControllerAdvice
class RestExceptionHandler {

    @ResponseBody
    @ExceptionHandler(WrongPhoneNumberFormat::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun wrongPhoneFormat(ex: WrongPhoneNumberFormat) = HitResponse.Error("Wrong phone number format")

    @ResponseBody
    @ExceptionHandler(TokenExpiredException::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    fun tokenExpired(ex: TokenExpiredException) = HitResponse.Error("Token expired or wrong")

    @ResponseBody
    @ExceptionHandler(FaultLogin::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    fun faultLogin(ex: FaultLogin) = HitResponse.Error("Login or password are wrong")

    @ResponseBody
    @ExceptionHandler(EmailAlreadyConfirmed::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    fun emailAlreadyConfirmed(ex: EmailAlreadyConfirmed) = HitResponse.Error("This email already registered by another user")

    @ResponseBody
    @ExceptionHandler(PhoneAlreadyConfirmed::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    fun phoneAlreadyConfirmed(ex: PhoneAlreadyConfirmed) = HitResponse.Error("This phone already registered by another user")

    @ResponseBody
    @ExceptionHandler(UserNotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun userNotFound(ex: UserPhoneRepo) = HitResponse.Error("User not found")
}