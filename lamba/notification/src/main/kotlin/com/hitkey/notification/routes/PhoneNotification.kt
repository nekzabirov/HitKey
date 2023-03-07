package com.hitkey.notification.routes

import com.hitkey.common.data.HitResponse
import com.hitkey.notification.model.SendSmsRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono
import java.util.function.Function

@Configuration
class PhoneNotification {

    @Bean
    fun sendSms(): (Mono<SendSmsRequest>) -> Mono<HitResponse> {
        return  {
            it.map {
                HitResponse.OK("Nek ok")
            }
        }
    }

    @Bean
    fun uppercase(): Function<Mono<String>, Mono<String>> {
        return Function { input: Mono<String> ->
            input.map { it.toUpperCase() }
        }
    }

}