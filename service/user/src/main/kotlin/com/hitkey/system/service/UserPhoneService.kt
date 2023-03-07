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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

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

    fun registerPhone(phoneNumber: String) = checkPhone(phone = phoneNumber)
        .map {
            hitCrypto.generateToken(
                hashMapOf(
                    Pair("phone", phoneNumber),
                    Pair("code", "1234")
                )
            )
        }

    fun attachTo(userID: Long, phoneNumber: String) = registerPhone(phoneNumber)
        .then(userPhoneRepo.findByPhoneNumber(phoneNumber))
        .defaultIfEmpty(
            UserPhone(
                phoneNumber = phoneNumber,
                confirmed = false,
                ownerID = userID
            )
        )
        .flatMap { userPhoneRepo.save(it) }
        .then(registerPhone(phoneNumber))

    fun confirm(phoneToken: String, code: String) = validateToken(phoneToken)
        .then(Mono.create {
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
        })
        .asFlow()
        .map { phone ->
            userPhoneRepo.findByPhoneNumber(phone).awaitFirstOrNull()
                ?.apply {
                confirmed = true
            }
                ?.run {
                userPhoneRepo.save(this).awaitFirst()
            }

            hitCrypto.generateToken(hashMapOf(Pair("phoneReal", phone)))
        }
        .asPublisher()
        .toMono()

    fun attachConfirmTo(userID: Long, phoneToken: String) = validateToken(phoneToken)
        .flatMap {
            val phoneNumber = hitCrypto.readClaims(phoneToken)["phoneReal"] as String

            checkPhone(phoneNumber).then(Mono.just(phoneNumber))
        }
        .map {phoneNumber ->
            UserPhone(
                phoneNumber = phoneNumber,
                confirmed = true,
                ownerID = userID
            )
        }
        .flatMap { userPhoneRepo.save(it) }

    private fun phoneAlreadyConfirmed(phone: String) = userPhoneRepo.existsByPhoneNumberAndConfirmed(
        phone, true
    )

    fun phoneAlreadyConfirmedByToken(phoneToken: String) = validateToken(phoneToken)
        .map { hitCrypto.readClaims(phoneToken)["phoneReal"] as String }
        .flatMap { phoneAlreadyConfirmed(it) }

    fun findConfirmed(phoneNumber: String) = phoneAlreadyConfirmed(phone = phoneNumber)
        .handle { t, u ->
            if (!t)
                u.error(NotFoundException())
            else
                u.next(t)
        }
        .then(userPhoneRepo.findByPhoneNumber(phoneNumber))
}