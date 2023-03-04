package com.hitkey.system.controller.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.hitkey.system.database.entity.user.UserGender
import java.time.LocalDate

data class UserUpdateRequest(
    @JsonProperty("first_name")
    val firstName: String?,

    @JsonProperty("last_name")
    val lastName: String?,

    val birthday: LocalDate?,

    @JsonProperty("sex")
    val gender: UserGender?,

    val avatar: String?
)
