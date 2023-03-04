package com.hitkey.system.database.repo

import com.hitkey.system.database.entity.user.UserAvatar
import com.hitkey.system.database.entity.user.UserEmail
import com.hitkey.system.database.entity.user.UserEntity
import com.hitkey.system.database.entity.user.UserPhone
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserRepo : ReactiveCrudRepository<UserEntity, Long>

interface UserPhoneRepo: ReactiveCrudRepository<UserPhone, Long> {
    fun existsByPhoneNumberAndConfirmed(phoneNumber: String, confirmed: Boolean): Mono<Boolean>

    fun findByPhoneNumber(phoneNumber: String): Mono<UserPhone>

    fun findByPhoneNumberAndConfirmed(phoneNumber: String, confirmed: Boolean): Mono<UserPhone>

    fun findByOwnerID(ownerID: Long): Flux<UserPhone>

    @Modifying
    @Query("update user_phone set confirmed = true where phone_number = :phone")
    fun confirm(phone: String)
}

interface UserEmailRepo: ReactiveCrudRepository<UserEmail, Long> {
    fun existsByEmailAndConfirmed(email: String, confirmed: Boolean): Mono<Boolean>

    fun findByEmail(email: String): Mono<UserEmail>

    fun findByEmailAndConfirmed(email: String, confirmed: Boolean): Mono<UserEmail>

    fun findByOwnerID(ownerID: Long): Flux<UserEmail>

    @Modifying
    @Query("update user_email set confirmed = true where email = :email")
    fun confirm(email: String)
}

interface UserAvatarRepo: ReactiveCrudRepository<UserAvatar, Long> {
    @Modifying
    @Query("update user_avatar set active = false where owner_id = :userID")
    fun disPrimaryBy(userID: Long): Mono<Void>

    fun findAllByOwnerID(ownerID: Long): Flux<UserAvatar>
}