package com.hitkey.system.component

import com.hitkey.system.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager: ReactiveAuthenticationManager {
    @Autowired
    private lateinit var crypto: HitCrypto

    @Autowired
    private lateinit var userService: UserService
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.credentials.toString()

        return Mono.just(crypto.validateToken(authToken))
            .filter { it }
            .switchIfEmpty(Mono.empty())
            .map { crypto.readUserID(authToken) }
            .map { it.toLong() }
            .publish { userService.findBy(it) }
            .map {
                UsernamePasswordAuthenticationToken(
                    it,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_${it.role}"))
                )
            }
    }
}