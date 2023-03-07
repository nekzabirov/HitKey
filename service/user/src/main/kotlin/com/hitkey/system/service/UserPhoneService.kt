package com.hitkey.system.service

import com.hitkey.common.component.HitCrypto
import com.hitkey.system.database.entity.user.UserEntity
import com.hitkey.system.database.entity.user.UserPhone
import com.hitkey.system.database.repo.UserPhoneRepo
import com.hitkey.system.exception.FaultPhoneConfirm
import com.hitkey.system.exception.TokenExpiredException
import com.hitkey.system.exception.WrongPhoneNumberFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserPhoneService {
    @Autowired
    private lateinit var userPhoneRepo: UserPhoneRepo

    @Autowired
    private lateinit var hitCrypto: HitCrypto

    private fun phoneParamRegxCheck(phone: String): Mono<Boolean> = Mono.create { u ->
        if (phone.isBlank())
            u.error(WrongPhoneNumberFormat())
        else if (!phone.startsWith("+"))
            u.error(WrongPhoneNumberFormat())
        else if (!phone.replace("+", "").all { it.isDigit() })
            u.error(WrongPhoneNumberFormat())
        else
            u.success(true)
    }

    private fun createPhoneToken(phoneNumber: String) = hitCrypto.generateToken(
        hashMapOf(
            Pair("phone", phoneNumber),
            Pair("code", "1234")
        )
    )

    fun registerPhone(phoneNumber: String): Mono<String> = phoneParamRegxCheck(phoneNumber).handle { _, u ->
        u.next(createPhoneToken(phoneNumber))
    }

    fun confirmPhoneToken(phoneToken: String, code: String): Mono<String> = Mono
        .create {
            if (!hitCrypto.validateToken(phoneToken)) {
                it.error(TokenExpiredException())

                return@create
            }

            val claims = hitCrypto.readClaims(phoneToken)

            if (!claims.containsKey("code") || !claims.containsKey("phone")) {
                it.error(TokenExpiredException())

                return@create
            }

            if (claims["code"] != code) {
                it.error(FaultPhoneConfirm())
                return@create
            }

            val phone = claims["phone"] as String

            it.success(phone)
        }
        .map {
            hitCrypto.generateToken(hashMapOf(Pair("phoneReal", it)))
        }

    fun attachPhoneToUser(userEntity: UserEntity, phoneNumber: String, isConfirmed: Boolean = false) =
        phoneParamRegxCheck(phoneNumber)
            .then(userPhoneRepo.findByPhoneNumber(phoneNumber))
            .defaultIfEmpty(
                UserPhone(
                    phoneNumber = phoneNumber,
                    confirmed = isConfirmed,
                    ownerID = userEntity.id
                )
            )
            .flatMap {
                userPhoneRepo.save(it.apply {
                    ownerID = userEntity.id
                    confirmed = isConfirmed
                })
            }
            .then(registerPhone(phoneNumber))

    fun attachConfirmPhoneToUser(userEntity: UserEntity, phoneToken: String) = Mono
        .create {
            if (!hitCrypto.validateToken(phoneToken)) {
                it.error(TokenExpiredException())
                return@create
            }

            val claims = hitCrypto.readClaims(phoneToken)

            if (!claims.containsKey("phoneReal")) {
                it.error(TokenExpiredException())
                return@create
            }

            val phone = claims["phoneReal"] as String

            it.success(phone)
        }
        .flatMap { phone ->
            attachPhoneToUser(userEntity, phone, true).map {
                phone to it
            }
        }

    fun findConfirmed(phoneNumber: String) = userPhoneRepo.findByPhoneNumberAndConfirmed(
        phoneNumber = phoneNumber,
        confirmed = true
    )

    fun exitsBy(phone: String, isConfirmed: Boolean) = userPhoneRepo.existsByPhoneNumberAndConfirmed(
        phone, isConfirmed
    )

    fun exitsByToken(phoneToken: String, isConfirmed: Boolean) = Mono
        .create {
            if (!hitCrypto.validateToken(phoneToken)) {
                it.error(TokenExpiredException())
                return@create
            }

            val claims = hitCrypto.readClaims(phoneToken)

            if (!claims.containsKey("phoneReal")) {
                it.error(TokenExpiredException())
                return@create
            }

            val phone = claims["phoneReal"] as String

            it.success(phone)
        }
        .flatMap { phone ->
            userPhoneRepo.existsByPhoneNumberAndConfirmed(phone, isConfirmed)
        }
}