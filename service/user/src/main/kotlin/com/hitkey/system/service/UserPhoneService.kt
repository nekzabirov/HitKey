package com.hitkey.system.service

import com.hitkey.common.component.HitCrypto
import com.hitkey.common.config.NotFoundException
import com.hitkey.common.data.UserPhoneDTO
import com.hitkey.system.database.entity.user.UserPhone
import com.hitkey.system.database.repo.UserPhoneRepo
import com.hitkey.system.exception.FaultPhoneConfirm
import com.hitkey.system.exception.PhoneAlreadyConfirmed
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

    private fun validateToken(phoneToken: String) = Mono.create {
        if (!hitCrypto.validateToken(phoneToken))
            it.error(TokenExpiredException())
        else
            it.success(phoneToken)
    }

    private fun phoneAlreadyConfirmed(phone: String) = userPhoneRepo.existsByPhoneNumberAndConfirmed(
        phone, true
    )

    fun phoneAlreadyConfirmedByToken(phoneToken: String) = validateToken(phoneToken)
        .map { hitCrypto.readClaims(phoneToken)["phoneReal"] as String }
        .flatMap { phoneAlreadyConfirmed(it) }

    private fun checkPhone(phone: String) = Mono
        .create { u ->
            if (phone.isBlank())
                u.error(WrongPhoneNumberFormat())
            else if (!phone.startsWith("+"))
                u.error(WrongPhoneNumberFormat())
            else if (!phone.replace("+", "").all { it.isDigit() })
                u.error(WrongPhoneNumberFormat())
            else
                u.success(true)
        }
        .then(phoneAlreadyConfirmed(phone))
        .handle { t, u ->
            if (t)
                u.error(PhoneAlreadyConfirmed())
            else
                u.next(true)
        }

    fun registerPhoneTo(userID: Long, phoneNumber: String) = checkPhone(phone = phoneNumber)
        .then(userPhoneRepo.findByPhoneNumber(phoneNumber))
        .defaultIfEmpty(
            UserPhone(
                phoneNumber = phoneNumber,
                confirmed = false,
                ownerID = userID
            )
        )
        .flatMap {
            userPhoneRepo.save(it.apply {
                ownerID = userID
            })
        }
        .then(
            Mono.just(
                hitCrypto.generateToken(
                    hashMapOf(
                        Pair("phone", phoneNumber),
                        Pair("code", "1234")
                    )
                )
            )
        )

    fun registerPhone(phoneNumber: String) = registerPhoneTo(0, phoneNumber)

    fun confirmPhone(phoneToken: String, code: String) = validateToken(phoneToken)
        .flatMap {
            Mono.create {
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
        }
        .flatMap { userPhoneRepo.findByPhoneNumber(it) }
        .switchIfEmpty(Mono.error(NotFoundException()))
        .handle { t, u ->
            if (t.confirmed)
                u.error(PhoneAlreadyConfirmed())
            else
                u.next(t)
        }
        .flatMap {
            userPhoneRepo.save(it.apply {
                confirmed = true
            })
        }
        .map {
            hitCrypto.generateToken(hashMapOf(Pair("phoneReal", it.phoneNumber)))
        }

    fun attachPhoneTo(userID: Long, phoneToken: String) = validateToken(phoneToken)
        .map { hitCrypto.readClaims(phoneToken)["phoneReal"] as String }
        .flatMap { userPhoneRepo.findByPhoneNumber(it) }
        .switchIfEmpty(Mono.error(NotFoundException()))
        .flatMap {
            userPhoneRepo.save(it.apply {
                ownerID = userID
            })
        }
        .map { UserPhoneDTO(it.phoneNumber, it.confirmed) }

    fun findConfirmed(phoneNumber: String) = phoneAlreadyConfirmed(phone = phoneNumber)
        .handle { t, u ->
            if (!t)
                u.error(NotFoundException())
            else
                u.next(t)
        }
        .then(userPhoneRepo.findByPhoneNumber(phoneNumber))
}