package com.hitkey.system.controller

import com.hitkey.system.controller.rest.*
import com.hitkey.system.database.entity.user.UserEntity
import com.hitkey.system.exception.EmailAlreadyConfirmed
import com.hitkey.system.exception.PhoneAlreadyConfirmed
import com.hitkey.system.service.FileService
import com.hitkey.system.service.UserEmailService
import com.hitkey.system.service.UserPhoneService
import com.hitkey.system.service.UserService
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactor.awaitSingle
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

    @GetMapping("info")
    fun mInfo() = userService
        .findBy(info().id)

    @PutMapping("update")
    fun update(@RequestBody payload: UserUpdateRequest) = userService.update(info().id,
            firstName = payload.firstName,
            lastName = payload.lastName,
            birthday = payload.birthday,
            gender = payload.gender
        ).then(Mono.just(true)).asFlow().combine(
            if (payload.avatar != null) userService.addAvatarForUser(info().id, payload.avatar).asFlow()
            else Mono.just(true).asFlow()
        ) { _, _ ->
            true
        }.asPublisher().toMono()

    @GetMapping("image/{fileID}", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun image(@PathVariable fileID: String) = fileService.findUserFile(fileID)

    @PutMapping("attach/phone")
    fun attachPhone(@RequestBody payload: RegisterPhoneRequest) = userPhoneService
        .registerPhoneTo(userID = info().id, payload.phoneNumber)
        .map { TokenResponse(it) }

    @PutMapping("attach/phone/confirm")
    fun attachPhone(@RequestBody payload: ConfirmPhoneRequest) = userPhoneService
        .confirmPhone(payload.token, payload.code)
        .map { true }

    @PutMapping("attach/email")
    fun attachEmail(@RequestBody payload: RegisterEmailRequest) = userEmailService
        .existBy(payload.email, true)
        .handle { t, u ->
            if (t)
                u.error(EmailAlreadyConfirmed())
            else
                u.next(t)
        }
        .then(userEmailService.attachTo(info(), payload.email, false))
        .map { TokenResponse(it) }

    @PutMapping("attach/email/confirm")
    fun attachEmailConfirm(@RequestBody payload: ConfirmEmailRequest) = userEmailService
        .confirmEmailToken(payload.token)
        .flatMap {
            userEmailService.confirmEmail(info(), it)
        }
        .map { true }

    fun info() = SecurityContextHolder
        .getContext()
        .authentication
        .principal as UserEntity

}