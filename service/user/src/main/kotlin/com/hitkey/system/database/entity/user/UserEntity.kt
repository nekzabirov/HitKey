package com.hitkey.system.database.entity.user

import com.hitkey.system.database.BaseEntity
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

enum class UserGender {
    MALE, FEMALE
}

enum class Role {
    ADMIN,
    USER
}

@Table(name = "user_data")
data class UserEntity(
    var firstName: String,

    var lastName: String,

    val password: String,

    var birthday: LocalDate,

    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    var gender: UserGender,

    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val role: Role = Role.USER,
): BaseEntity()