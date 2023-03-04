package com.hitkey.system.controller

import com.hitkey.common.component.HitCrypto
import com.hitkey.system.controller.rest.*
import com.hitkey.system.database.entity.user.UserContact
import com.hitkey.system.exception.EmailAlreadyConfirmed
import com.hitkey.system.exception.FaultLogin
import com.hitkey.common.config.ParamIsRequired
import com.hitkey.system.exception.PhoneAlreadyConfirmed
import com.hitkey.system.service.UserEmailService
import com.hitkey.system.service.UserPhoneService
import com.hitkey.system.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("auth")
class AuthController {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userPhoneService: UserPhoneService

    @Autowired
    private lateinit var userEmailService: UserEmailService

    @Autowired
    private lateinit var crypto: HitCrypto

    @PostMapping("phone/register/step/1")
    fun register(@RequestBody payload: RegisterPhoneRequest) = Mono.create<TokenResponse> {
        if (payload.phoneNumber.isBlank())
            it.error(ParamIsRequired("phone token should be fill"))
        else
            it.success()
    }
        .then(userPhoneService.exitsBy(payload.phoneNumber, isConfirmed = true))
        .handle { t, u ->
            if (t)
                u.error(PhoneAlreadyConfirmed())
            else
                u.next(t)
        }
        .then(userPhoneService.registerPhone(payload.phoneNumber))
        .map { TokenResponse(it) }

    @PostMapping("phone/register/step/2")
    fun register(@RequestBody payload: ConfirmPhoneRequest) = Mono.create<TokenResponse> {
        if (payload.token.isBlank())
            it.error(ParamIsRequired("phone_token should be fill"))
        else if (payload.code.isBlank())
            it.error(ParamIsRequired("code should be fill"))
        else
            it.success()
    }
        .then(userPhoneService.confirmPhoneToken(payload.token, payload.code))
        .map { TokenResponse(it) }

    @PostMapping("phone/register/step/3")
    fun register(@RequestBody payload: RegisterUserRequest.Phone) = payload.isFilled
        .then(Mono.create<TokenResponse> {
            if (payload.phoneToken.isBlank())
                it.error(ParamIsRequired("phone_token is required"))
            else
                it.success()
        })
        .then(
            userService.register(
                firstName = payload.firstName,
                lastName = payload.lastName,
                password = payload.password,
                gender = payload.gender,
                birthday = payload.birthday
            )
        )
        .flatMap {
            userPhoneService.attachConfirmPhoneToUser(
                userEntity = it,
                phoneToken = payload.phoneToken
            )
        }
        .flatMap {
            login(
                LoginViaPhoneRequest(
                    phoneNumber = it.first,
                    password = payload.password
                )
            )
        }

    @PostMapping("email/register/step/1")
    fun register(@RequestBody payload: RegisterEmailRequest) = Mono.create<TokenResponse> {
        if (payload.email.isBlank())
            it.error(ParamIsRequired("email should be fill"))
        else
            it.success()
    }
        .then(userEmailService.existBy(payload.email, isConfirmed = true))
        .handle { t, u ->
            if (t)
                u.error(EmailAlreadyConfirmed())
            else
                u.next(t)
        }
        .then(userEmailService.registerEmail(payload.email))
        .map { TokenResponse(it) }

    @PostMapping("email/register/step/2")
    fun register(@RequestBody payload: ConfirmEmailRequest) = Mono.create<TokenResponse> {
        if (payload.token.isBlank())
            it.error(ParamIsRequired("phone_token should be fill"))
        else
            it.success()
    }
        .then(userEmailService.confirmEmailToken(payload.token))
        .map { TokenResponse(it) }

    @PostMapping("email/register/step/3")
    fun register(@RequestBody payload: RegisterUserRequest.Email) = payload.isFilled
        .then(Mono.create<TokenResponse> {
            if (payload.emailToken.isBlank())
                it.error(ParamIsRequired("email_token is required"))
            else
                it.success()
        })
        .then(
            userService.register(
                firstName = payload.firstName,
                lastName = payload.lastName,
                password = payload.password,
                gender = payload.gender,
                birthday = payload.birthday
            )
        )
        .flatMap {
            userEmailService.confirmEmail(
                user = it,
                emailToken = payload.emailToken
            )
        }
        .flatMap {
            login(LoginViaEmailRequest(email = it.first, password = payload.password))
        }

    @PostMapping("phone/login")
    fun login(@RequestBody payload: LoginViaPhoneRequest) = userPhoneService
        .findConfirmed(payload.phoneNumber)
        .login(payload.password)

    @PostMapping("email/login")
    fun login(@RequestBody payload: LoginViaEmailRequest) = userEmailService
        .findConfirmed(payload.email)
        .login(payload.password)

    private fun <T : UserContact> Mono<T>.login(password: String) = this
        .switchIfEmpty(Mono.error(FaultLogin()))
        .map { it.ownerID }
        .publish {
            userService.findBy(it)
        }
        .switchIfEmpty(Mono.error(FaultLogin()))
        .handle { t, u ->
            if (!crypto.matches(password, t.password))
                u.error(FaultLogin())
            else
                u.next(t)
        }
        .map {
            crypto.generateAuthToken(it.id)
        }
        .map { TokenResponse(it) }
}