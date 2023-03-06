package com.hitkey.system.service

import com.hitkey.common.component.HitCrypto
import com.hitkey.common.data.UserAvatarDTO
import com.hitkey.common.data.UserDTO
import com.hitkey.common.data.UserEmailDTO
import com.hitkey.common.data.UserPhoneDTO
import com.hitkey.system.database.entity.user.UserAvatar
import com.hitkey.system.database.entity.user.UserEntity
import com.hitkey.system.database.entity.user.UserGender
import com.hitkey.system.database.repo.UserAvatarRepo
import com.hitkey.system.database.repo.UserEmailRepo
import com.hitkey.system.database.repo.UserPhoneRepo
import com.hitkey.system.database.repo.UserRepo
import com.hitkey.system.exception.UserNotFound
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitSingle
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDate

@Service
class UserService {
    @Autowired
    private lateinit var userRepo: UserRepo

    @Autowired
    private lateinit var userPhoneRepo: UserPhoneRepo

    @Autowired
    private lateinit var userEmailRepo: UserEmailRepo

    @Autowired
    private lateinit var crypto: HitCrypto

    @Autowired
    private lateinit var fileService: FileService

    fun register(
        firstName: String, lastName: String, birthday: LocalDate, gender: UserGender, password: String
    ) = UserEntity(
        firstName = firstName,
        lastName = lastName,
        birthday = birthday,
        password = crypto.encodePassword(password),
        gender = gender
    ).run { userRepo.save(this) }

    fun findBy(id: Publisher<Long>) = userRepo.findById(id)

    fun findBy(id: Long) = Mono.zip(
        userPhoneRepo.findByOwnerID(id).collectList(),
        userEmailRepo.findByOwnerID(id).collectList()
    ).flatMap { contacts ->
        userRepo
            .findById(id)
            .map { user ->
                UserDTO(
                    id = user.id,

                    firstName = user.firstName,
                    lastName = user.lastName,

                    birthDay = user.birthday,

                    gender = when (user.gender) {
                        UserGender.MALE -> com.hitkey.common.data.UserGender.MALE
                        else -> com.hitkey.common.data.UserGender.FEMALE
                    },

                    phones = contacts.t1.map {
                        UserPhoneDTO(
                            phoneNumber = it.phoneNumber,
                            confirmed = it.confirmed
                        )
                    },
                    emails = contacts.t2.map {
                        UserEmailDTO(
                            email = it.email,
                            confirmed = it.confirmed
                        )
                    },

                    avatar = user.avatar
                )
            }
    }

    fun addAvatarForUser(userID: Long, image: String) = userRepo
        .findById(userID)
        .switchIfEmpty(Mono.error(UserNotFound()))
        .asFlow()
        .map {
            val fileID = fileService.addUserFile(image).awaitSingle().data

            it.apply {
                avatar = fileID
            }
        }
        .asPublisher()
        .toMono()
        .flatMap {
            userRepo.save(it)
        }
        .map {
            true
        }

    fun update(
        userID: Long,
        firstName: String?,
        lastName: String?,
        birthday: LocalDate?,
        gender: UserGender?
    ) = userRepo
            .findById(userID)
            .map {
                it.apply {
                    if (firstName != null)
                        this.firstName = firstName
                    if (lastName != null)
                        this.lastName = lastName
                    if (birthday != null)
                        this.birthday = birthday
                    if (gender != null)
                        this.gender = gender
                }
            }
            .flatMap {
                userRepo.save(it)
            }
}