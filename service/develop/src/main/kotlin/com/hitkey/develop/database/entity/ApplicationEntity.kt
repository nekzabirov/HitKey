package com.hitkey.develop.database.entity

import com.hitkey.develop.database.BaseEntity
import org.springframework.data.relational.core.mapping.Table

@Table(name = "app_data")
data class ApplicationEntity(
    var name: String,

    val token: String,

    var logoID: String,

    val ownerID: Long
): BaseEntity()
