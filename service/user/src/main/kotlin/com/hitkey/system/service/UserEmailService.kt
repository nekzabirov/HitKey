package com.hitkey.system.service

import com.hitkey.system.component.HitCrypto
import com.hitkey.system.database.entity.user.UserEmail
import com.hitkey.system.database.entity.user.UserEntity
import com.hitkey.system.database.repo.UserEmailRepo
import com.hitkey.system.exception.TokenExpiredException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserEmailService {
    @Autowired
    private lateinit var userEmailRepo: UserEmailRepo

    @Autowired
    private lateinit var hitCrypto: HitCrypto

    fun checkEmailReq(email: String) = Mono.just(true)

    fun registerEmail(email: String) = checkEmailReq(email)
        .then(
            Mono.just(hitCrypto.generateToken(
                hashMapOf(
                    Pair("email", email)
                )
            ))
        )

    fun confirmEmailToken(emailToken: String) = Mono
        .just(hitCrypto.validateToken(emailToken))
        .then(Mono.just(hitCrypto.readClaims(emailToken)))
        .handle { claims, u ->
            if (!claims.containsKey("email")) {
                u.error(TokenExpiredException())

                return@handle
            }
            val email = claims["email"] as String

            userEmailRepo.confirm(email)

            u.next(hitCrypto.generateToken(hashMapOf(
                "emailReal" to email
            )))
        }

    fun attachTo(user: UserEntity, email: String, isConfirmed: Boolean) = userEmailRepo.findByEmail(email)
        .defaultIfEmpty(
            UserEmail(
            email = email,
            confirmed = false,
            ownerID = user.id
        )
        )
        .map {
            it.apply {
                ownerID = user.id
                confirmed = isConfirmed
            }
        }
        .flatMap { userEmailRepo.save(it) }
        .then(registerEmail(email))

    fun confirmEmail(user: UserEntity, emailToken: String) = Mono
        .just(hitCrypto.validateToken(emailToken))
        .then(Mono.just(hitCrypto.readClaims(emailToken)))
        .handle { claims, u ->
            if (!claims.containsKey("emailReal")) {
                u.error(TokenExpiredException())

                return@handle
            }

            val email = claims["emailReal"] as String

            u.next(email)
        }
        .flatMap { email ->
            attachTo(user = user, email = email, isConfirmed = true).map {
                email to it
            }
        }

    fun findConfirmed(email: String) = userEmailRepo.findByEmailAndConfirmed(
        email = email, confirmed = true
    )

    fun existBy(email: String, isConfirmed: Boolean) = userEmailRepo.existsByEmailAndConfirmed(email, isConfirmed)
}