package com.hitkey.system.database.entity.user

import com.hitkey.system.database.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

interface UserContact {
    val confirmed: Boolean

    val ownerID: Long
}

@Table(name = "user_phone")
data class UserPhone(
    val phoneNumber: String,

    @Column("confirmed")
    override var confirmed: Boolean,

    override var ownerID: Long,
): UserContact, BaseEntity()

@Table(name = "user_email")
data class UserEmail(
    val email: String,

    @Column("confirmed")
    override var confirmed: Boolean,

    override var ownerID: Long,
): UserContact, BaseEntity()

