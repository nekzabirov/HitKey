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
    private lateinit var crypto: HitCrypto

    @PostMapping("phone/register/step/1")
    fun register(@RequestBody payload: RegisterPhoneRequest) = Mono
        .create<TokenResponse> {
            if (payload.phoneNumber.isBlank())
                it.error(ParamIsRequired("phone should be fill"))
            else
                it.success()
        }
        .then(userPhoneService.registerPhone(payload.phoneNumber))
        .map { TokenResponse(it) }

    @PostMapping("phone/register/step/2")
    fun register(@RequestBody payload: ConfirmPhoneRequest) = Mono
        .create<TokenResponse> {
            if (payload.token.isBlank())
                it.error(ParamIsRequired("phone_token should be fill"))
            else if (payload.code.isBlank())
                it.error(ParamIsRequired("code should be fill"))
            else
                it.success()
        }
        .then(userPhoneService.confirm(payload.token, payload.code))
        .map { TokenResponse(it) }

    @PostMapping("phone/register/step/3")
    fun register(@RequestBody payload: RegisterUserRequest.Phone) = payload.isFilled
        .then(Mono.create<TokenResponse> {
            if (payload.phoneToken.isBlank())
                it.error(ParamIsRequired("phone_token is required"))
            else
                it.success()
        })
        .then(userPhoneService.phoneAlreadyConfirmedByToken(payload.phoneToken))
        .handle { t, u ->
            if (t)
                u.error(PhoneAlreadyConfirmed())
            else
                u.next(true)
        }
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
            userPhoneService.attachConfirmTo(
                userID = it.id,
                phoneToken = payload.phoneToken
            )
        }
        .flatMap {
            login(
                LoginViaPhoneRequest(
                    phoneNumber = it.phoneNumber,
                    password = payload.password
                )
            )
        }

    @PostMapping("phone/login")
    fun login(@RequestBody payload: LoginViaPhoneRequest) = userPhoneService
        .findConfirmed(payload.phoneNumber)
        .switchIfEmpty(Mono.error(FaultLogin()))
        .map { it.ownerID }
        .publish { userService.findBy(it) }
        .handle { t, u ->
            if (!crypto.matches(payload.password, t.password))
                u.error(FaultLogin())
            else
                u.next(t)
        }
        .map { crypto.generateAuthToken(it.id) }
        .map { TokenResponse(it) }
}