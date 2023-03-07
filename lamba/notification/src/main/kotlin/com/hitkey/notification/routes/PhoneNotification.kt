package com.hitkey.notification.routes

import com.hitkey.common.data.HitResponse
import com.hitkey.notification.model.SendSmsRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class PhoneNotification {

    @Bean
    fun sendSms(): (Mono<SendSmsRequest>) -> Mono<HitResponse> {
        return  {
            it.map {
                HitResponse.OK(null)
            }
        }
    }

}