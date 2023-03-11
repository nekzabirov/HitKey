package com.hitkey.file.component

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import org.springframework.security.core.context.SecurityContextImpl

@Component
class SecurityContextRepository: ServerSecurityContextRepository {
    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    override fun save(exchange: ServerWebExchange?, context: SecurityContext?): Mono<Void> {
        throw IllegalStateException("Save method not supported!");
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        val authHeader = exchange.request
            .headers
            .getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return Mono.empty()

        val authToken = authHeader.substring(7)

        val auth = UsernamePasswordAuthenticationToken(authToken, authToken)

        return authenticationManager.authenticate(auth)
            .doOnNext {
                SecurityContextHolder.getContext().authentication = it
            }
            .map { SecurityContextImpl(it) }
    }
}