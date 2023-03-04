package com.hitkey.common.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

enum class UserGender {
    MALE, FEMALE
}

data class UserPhoneDTO(
    //@JsonProperty("phone_number")
    val phoneNumber: String,

    @JsonProperty("confirmed")
    val isConfirmed: Boolean
)

data class UserEmailDTO(
    val email: String,

    @JsonProperty("confirmed")
    val isConfirmed: Boolean
)

data class UserAvatarDTO(
    //@JsonProperty("file_id")
    val fileID: String,

    val primary: Boolean
)

data class UserDTO(
    val id: Long,

    val firstName: String,
    val lastName: String,

    val birthDay: LocalDate,

    //@JsonProperty("sex")
    val gender: UserGender,

    val phones: List<UserPhoneDTO>,

    val emails: List<UserEmailDTO>,

    val avatar: List<UserAvatarDTO>
)
