package com.hitkey.file.component

import com.hitkey.file.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager: ReactiveAuthenticationManager {
    @Autowired
    private lateinit var userService: UserService

    override fun authenticate(authentication: Authentication): Mono<Authentication> = Mono.just(authentication.credentials.toString())
        .flatMap { userService.userBy(it) }
        .map {
            UsernamePasswordAuthenticationToken(
                it,
                null,
                emptyList()
            )
        }
}