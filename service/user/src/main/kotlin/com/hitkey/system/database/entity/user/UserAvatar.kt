package com.hitkey.system.database.entity.user

import com.hitkey.system.database.BaseEntity
import org.springframework.data.relational.core.mapping.Table

@Table(name = "user_avatar")
data class UserAvatar(
    val fileID: String,

    var active: Boolean,

    val ownerID: Long
): BaseEntity()
