package com.hitkey.system.controller

import com.hitkey.system.controller.rest.UserUpdateRequest
import com.hitkey.system.database.entity.user.UserEntity
import com.hitkey.system.service.FileService
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

    fun info() = SecurityContextHolder
        .getContext()
        .authentication
        .principal as UserEntity

}