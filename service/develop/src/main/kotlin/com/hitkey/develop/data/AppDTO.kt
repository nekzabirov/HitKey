package com.hitkey.develop.data

import com.hitkey.develop.database.entity.ApplicationEntity
import org.springframework.stereotype.Component

data class AppDTO(
    val name: String,
    val logo: String,
    val token: String
)

@Component
class AppDTOMapper : java.util.function.Function<ApplicationEntity, AppDTO> {
    override fun apply(t: ApplicationEntity): AppDTO = AppDTO(
        name = t.name,
        logo = t.logoID,
        token = t.token
    )
}