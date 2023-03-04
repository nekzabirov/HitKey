package com.hitkey.system.controller.rest

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginViaPhoneRequest(
    @JsonProperty("phone_number")
    val phoneNumber: String,

    val password: String
)

data class LoginViaEmailRequest(
    val email: String,

    val password: String
)