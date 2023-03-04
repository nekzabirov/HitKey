package com.hitkey.common.config

import com.hitkey.common.data.HitResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestHandler {
    @ResponseBody
    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun wrongPhoneFormat(ex: NotFoundException) = HitResponse.Error("Not found")

    @ResponseBody
    @ExceptionHandler(UnAuthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun nnAuthorizedException(ex: UnAuthorizedException) = HitResponse.Error("unauthorized")

    @ResponseBody
    @ExceptionHandler(ParamIsRequired::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun paramIsRequired(ex: ParamIsRequired) = HitResponse.Error(ex.message)
}