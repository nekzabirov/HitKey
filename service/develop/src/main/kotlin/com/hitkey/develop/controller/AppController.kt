package com.hitkey.develop.controller

import com.hitkey.common.config.ParamIsRequired
import com.hitkey.common.data.UserDTO
import com.hitkey.develop.data.CreateAppModel
import com.hitkey.develop.service.AppService
import com.hitkey.develop.service.FileService
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
@RequestMapping("app")
class AppController {
    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var appService: AppService

    @PostMapping("create")
    fun create(@RequestBody payload: CreateAppModel) = flow {
        if (payload.name.isNullOrBlank())
            throw ParamIsRequired("name is required")
        else if (payload.logo.isNullOrBlank())
            throw ParamIsRequired("logo is required in base64 format")

        val logoID = fileService.addUserFile(payload.logo).awaitSingle()

        appService.create(
            ownerID = user.id,
            name = payload.name,
            logoID = logoID!!
        ).awaitSingle().run {
            emit(this)
        }
    }
        .asPublisher()
        .toMono()
        .map {
            it.token
        }

    @PutMapping("{token}")
    fun update(@PathVariable token: String, @RequestBody payload: CreateAppModel) = flow {
        if (payload.name != null && payload.name.isBlank())
            throw ParamIsRequired("name is required")
        if (payload.logo != null && payload.logo.isBlank())
            throw ParamIsRequired("logo is required in base64 format")

        val logoID = if (payload.logo != null)
            fileService.addUserFile(payload.logo).awaitSingle()
        else
            null

        appService.update(
            ownerID = user.id,
            appToken = token,
            name = payload.name,
            logoID = logoID
        ).awaitSingle().run {
            emit(this)
        }
    }
        .asPublisher()
        .toMono()
        .map {
            true
        }

    @DeleteMapping("{token}")
    fun delete(@PathVariable token: String) = appService
        .delete(ownerID = user.id, appToken = token)
        .map {
            true
        }

    @GetMapping("{token}/info")
    fun info(@PathVariable token: String) = appService
        .infoBy(token)

    @GetMapping("list")
    fun list() = appService.listOf(user.id)

    private val user
        get() = SecurityContextHolder
            .getContext()
            .authentication
            .principal as UserDTO
}