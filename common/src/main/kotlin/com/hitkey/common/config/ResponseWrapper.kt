package com.hitkey.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.hitkey.common.data.HitResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.accept.RequestedContentTypeResolver
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestControllerAdvice
class ResponseWrapper(
    serverCodecConfigurer: ServerCodecConfigurer,
    resolver: RequestedContentTypeResolver
) :
    ResponseBodyResultHandler(serverCodecConfigurer.writers, resolver) {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    override fun supports(result: HandlerResult): Boolean {
        if (result.returnType.resolve() != Mono::class.java &&
            result.returnType.resolve() != Flux::class.java
        )
            return false

        return true
    }

    @Throws(ClassCastException::class)
    override fun handleResult(exchange: ServerWebExchange, result: HandlerResult): Mono<Void> {
        val body = when (val value = result.returnValue) {
            is Mono<*> -> value
            is Flux<*> -> value.collectList()
            else -> throw ClassCastException("The \"body\" should be Mono<*> or Flux<*>!")
        }.map { r ->
            if (r is ByteArray) {
                exchange.response.headers.contentType = MediaType.IMAGE_JPEG
                r
            } else {
                exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                HitResponse.OK(r)
            }
        }.map {
            if (it is ByteArray)
                exchange.response.bufferFactory().wrap(it)
            else
                objectMapper.writeValueAsBytes(it).run {
                    exchange.response.bufferFactory().wrap(this)
                }
        }

        return exchange.response.writeWith(body)
    }

    companion object {
        @JvmStatic
        private fun methodForReturnType(): Mono<HitResponse.OK<Any>>? = null

        private val returnType: MethodParameter = MethodParameter(
            ResponseWrapper::class.java.getDeclaredMethod("methodForReturnType"), -1
        )
    }
}