package com.hitkey.file.service

import com.hitkey.common.config.UnAuthorizedException
import com.hitkey.common.data.HitResponse
import com.hitkey.common.data.UserDTO
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class UserService(private val webClientBuilder: WebClient.Builder) {
    fun userBy(token: String) = webClientBuilder.build().get()
        .uri("http://USER/api/v1/user")
        .headers {
            it.set(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
        .retrieve()
        .bodyToMono<HitResponse.OK<UserDTO>>()
        .onErrorResume {
            Mono.error(UnAuthorizedException())
        }
        .mapNotNull {
            it.data
        }

}