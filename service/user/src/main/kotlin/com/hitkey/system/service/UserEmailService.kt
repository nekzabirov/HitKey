package com.hitkey.system.service

import com.hitkey.common.component.HitCrypto
import com.hitkey.common.config.NotFoundException
import com.hitkey.system.database.entity.user.UserEmail
import com.hitkey.system.database.entity.user.UserEntity
import com.hitkey.system.database.repo.UserEmailRepo
import com.hitkey.system.exception.EmailAlreadyConfirmed
import com.hitkey.system.exception.TokenExpiredException
import com.hitkey.system.exception.WrongEmailFormat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Service
class UserEmailService {
    @Autowired
    private lateinit var userEmailRepo: UserEmailRepo

    @Autowired
    private lateinit var hitCrypto: HitCrypto

    private fun checkEmail(email: String) = flow {
        if (!Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$").matches(email))
            throw WrongEmailFormat()

        if (userEmailRepo.existsByEmailAndConfirmed(email, true).awaitFirst())
            throw EmailAlreadyConfirmed()

        emit(true)
    }.asPublisher().toMono()

    fun attachTo(userID: Long, email: String) = checkEmail(email)
        .then(userEmailRepo.findByEmail(email))
        .switchIfEmpty { Mono.just(
            UserEmail(
                email = email,
                confirmed = false,
                ownerID = userID
            )
        ) }
        .map {
            it.apply {
                ownerID = userID
            }
        }
        .flatMap { userEmailRepo.save(it) }
        .map {
            hitCrypto.generateToken(
                hashMapOf(
                    Pair("emailReal", email)
                )
            )
        }

    fun confirm(emailToken: String) = Mono
        .just(hitCrypto.validateToken(emailToken))
        .then(Mono.just(hitCrypto.readClaims(emailToken)))
        .map { it["emailReal"] as String }
        .flatMap { email -> userEmailRepo.findByEmail(email) }
        .switchIfEmpty { Mono.error(NotFoundException()) }
        .map { it.apply { confirmed = true } }
        .flatMap { userEmailRepo.save(it) }
}