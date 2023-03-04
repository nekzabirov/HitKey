package com.hitkey.file.config

import com.hitkey.common.HitResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

class ParamIsRequired(override val message: String): RuntimeException(message)

@RestControllerAdvice
class RestExceptionHandler {
    @ResponseBody
    @ExceptionHandler(ParamIsRequired::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun paramIsRequired(ex: ParamIsRequired) = HitResponse.Error(ex.message)
}