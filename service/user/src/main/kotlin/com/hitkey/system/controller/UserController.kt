package com.hitkey.system.controller

import com.hitkey.common.config.ParamIsRequired
import com.hitkey.common.data.UserDTO
import com.hitkey.system.controller.rest.*
import com.hitkey.system.database.entity.user.UserEntity
import com.hitkey.system.exception.EmailAlreadyConfirmed
import com.hitkey.system.service.FileService
import com.hitkey.system.service.UserEmailService
import com.hitkey.system.service.UserPhoneService
import com.hitkey.system.service.UserService
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.publish
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
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

    @GetMapping
    fun mInfo() = userService.findBy(mUser.id)

    @PutMapping
    fun updateN(@RequestBody payload: UserUpdateRequest) = Mono
        .create {
            if (payload.firstName != null && payload.firstName.isBlank())
                it.error(ParamIsRequired("firstName should be fill"))
            else if (payload.lastName != null && payload.lastName.isBlank())
                it.error(ParamIsRequired("lastName should be fill"))
            else if (payload.avatar != null && payload.avatar.isBlank())
                it.error(ParamIsRequired("lastName should be fill"))

            it.success(true)
        }
        .asFlow()
        .map {
            val user = mInfo().awaitSingle()

            val avatarID = if (payload.avatar != null) {
                val avatarID = fileService.saveImage(user.id, payload.avatar).awaitSingle()

                if (user.avatar != null)
                    fileService.removeImage(user.id, user.avatar!!).awaitFirst()

                avatarID
            } else
                user.avatar


            userService.update(
                userID = user.id,
                firstName = payload.firstName,
                lastName = payload.lastName,
                birthday = payload.birthday,
                gender = payload.gender,
                avatarID = avatarID
            ).awaitSingle()

            true
        }
        .asPublisher()
        .toMono()
        .then(mInfo())

    @PostMapping("attach/phone")
    fun attachPhone(@RequestBody payload: RegisterPhoneRequest) = userPhoneService
        .attachTo(userID = mUser.id, payload.phoneNumber)
        .map { TokenResponse(it) }

    @PostMapping("attach/phone/confirm")
    fun conformPhone(@RequestBody payload: ConfirmPhoneRequest) = userPhoneService
        .confirm(payload.token, payload.code)
        .map { true }

    @PostMapping("attach/email")
    fun attachEmail(@RequestBody payload: RegisterEmailRequest) = userEmailService
        .attachTo(mUser.id, payload.email)
        .map { TokenResponse(it) }

    @PostMapping("attach/email/confirm")
    fun attachEmailConfirm(@RequestBody payload: ConfirmEmailRequest) = userEmailService
        .confirm(payload.token)
        .map { true }
}