package com.hitkey.system.controller.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.hitkey.system.database.entity.user.UserGender
import com.hitkey.common.config.ParamIsRequired
import reactor.core.publisher.Mono
import java.time.LocalDate

open class RegisterUserRequest {
    @JsonProperty("first_name")
    val firstName: String = ""

    @JsonProperty("last_name")
    val lastName: String = ""

    val password: String = ""

    val birthday: LocalDate = LocalDate.now()

    @JsonProperty("sex")
    val gender: UserGender = UserGender.MALE

    class Phone(@JsonProperty("phone_token")
                val phoneToken: String
    ): RegisterUserRequest()

    data class Email(@JsonProperty("email_token")
                     val emailToken: String
    ): RegisterUserRequest()
}

val RegisterUserRequest.isFilled: Mono<Boolean>
    get() = Mono.create {
        if (this.password.length < 8)
            it.error(ParamIsRequired("password should be more than 8 length"))
        else if (this.firstName.isBlank())
            it.error(ParamIsRequired("first_name is required"))
        else if (this.firstName.contains(" "))
            it.error(ParamIsRequired("first_name shouldn't has spaces"))
        else if (this.lastName.isBlank())
            it.error(ParamIsRequired("last_name is required"))
        else if (this.lastName.contains(" "))
            it.error(ParamIsRequired("last_name shouldn't has spaces"))
        else
            it.success(true)
    }