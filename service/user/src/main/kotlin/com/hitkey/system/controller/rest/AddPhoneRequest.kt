package com.hitkey.system.controller.rest

import com.fasterxml.jackson.annotation.JsonProperty

data class RegisterPhoneRequest(
    @JsonProperty("phone_number")
    val phoneNumber: String
)

data class ConfirmPhoneRequest(
    @JsonProperty("phone_token")
    val token: String,

    val code: String
)
