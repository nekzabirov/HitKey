package com.hitkey.system.controller

import com.hitkey.system.controller.rest.*
import com.hitkey.system.database.entity.user.UserEntity
import com.hitkey.system.exception.EmailAlreadyConfirmed
import com.hitkey.system.service.FileService
import com.hitkey.system.service.UserEmailService
import com.hitkey.system.service.UserPhoneService
import com.hitkey.system.service.UserService
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.publish
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
@RequestMapping("user")
class UserController {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var userPhoneService: UserPhoneService

    @Autowired
    private lateinit var userEmailService: UserEmailService

    private val mUser
        get() = SecurityContextHolder
            .getContext()
            .authentication
            .principal as UserEntity

    @GetMapping("info")
    fun mInfo() = userService
        .findBy(mUser.id)

    @PutMapping("update")
    fun update(@RequestBody payload: UserUpdateRequest) = flow {
        val avatarID = if (payload.avatar != null)
            fileService.saveImage(payload.avatar).awaitFirst()
        else
            null

        val lastAvatarID = mUser.avatar

        userService.update(
            userID = mUser.id,
            firstName = payload.firstName,
            lastName = payload.lastName,
            birthday = payload.birthday,
            gender = payload.gender,
            avatarID = avatarID
        ).awaitFirst()

        if (avatarID != null && lastAvatarID != null)
            fileService.removeImage(lastAvatarID).awaitFirst()

        emit(mInfo().awaitFirst())
    }
        .asPublisher()
        .toMono()

    @PutMapping("attach/phone")
    fun attachPhone(@RequestBody payload: RegisterPhoneRequest) = userPhoneService
        .attachTo(userID = mUser.id, payload.phoneNumber)
        .map { TokenResponse(it) }

    @PutMapping("attach/phone/confirm")
    fun conformPhone(@RequestBody payload: ConfirmPhoneRequest) = userPhoneService
        .confirm(payload.token, payload.code)
        .map { true }

    @PutMapping("attach/email")
    fun attachEmail(@RequestBody payload: RegisterEmailRequest) = userEmailService
        .attachTo(mUser.id, payload.email)
        .map { TokenResponse(it) }

    @PutMapping("attach/email/confirm")
    fun attachEmailConfirm(@RequestBody payload: ConfirmEmailRequest) = userEmailService
        .confirm(payload.token)
        .map { true }
}