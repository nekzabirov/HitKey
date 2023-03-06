package com.hitkey.develop.service

import com.hitkey.common.data.HitResponse
import com.hitkey.common.config.UnAuthorizedException
import com.hitkey.common.data.UserDTO
import com.netflix.discovery.EurekaClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class UserService {
    @Autowired
    private lateinit var eurekaClient: EurekaClient

    private val webClient: WebClient
        get() {
            val homePageUrl = eurekaClient.getApplication("user_service").instances.run {
                if (this.isEmpty())
                    throw RuntimeException("file service isn't run")

                random()
            }.homePageUrl

            return WebClient.create(
                "https://$homePageUrl/user/"
            )
        }

    fun userBy(token: String) = webClient.get()
        .uri("info")
        .headers {
            it.set(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
        .retrieve()
        .onStatus(
            { status -> status.value() == HttpStatus.UNAUTHORIZED.value() },
            { _ -> Mono.error(UnAuthorizedException()) }
        )
        .bodyToMono<HitResponse.OK<UserDTO>>()
        .map { it.data }

}