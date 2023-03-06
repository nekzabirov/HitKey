package com.hitkey.common.data

import java.time.LocalDate

enum class UserGender {
    MALE, FEMALE
}

data class UserPhoneDTO(
    //@JsonProperty("phone_number")
    val phoneNumber: String = "",

    val confirmed: Boolean = false
)

data class UserEmailDTO(
    val email: String = "",

    val confirmed: Boolean = false
)

data class UserAvatarDTO(
    //@JsonProperty("file_id")
    val fileID: String = "",

    val primary: Boolean = false
)

data class UserDTO(
    val id: Long = 0,

    val firstName: String = "",
    val lastName: String = "",

    val birthDay: LocalDate = LocalDate.now(),

    //@JsonProperty("sex")
    val gender: UserGender = UserGender.MALE,

    val phones: List<UserPhoneDTO> = emptyList(),

    val emails: List<UserEmailDTO> = emptyList(),

    val avatar: List<UserAvatarDTO> = emptyList()
)
