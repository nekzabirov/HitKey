package com.hitkey.file.controller

import com.hitkey.common.config.ParamIsRequired
import com.hitkey.file.controller.rest.FileRequest
import com.hitkey.file.service.FileService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.Base64

@RestController
@RequestMapping("user")
class UserController {
    companion object {
        private const val FILE_DIR = "user"
    }

    @Autowired
    private lateinit var fileService: FileService

    @GetMapping("test")
    fun test() = Mono.just("Test")

    @PostMapping("image/save")
    fun saveImage(@RequestBody payload: FileRequest) = Mono
        .create {
            if (payload.file.isBlank())
                it.error(ParamIsRequired("file param in 64 require"))
            else
                it.success("")
        }
        .map { Base64.getDecoder().decode(payload.file) }
        .publish { fileService.saveFile(it, FILE_DIR) }

    @GetMapping("image/{id}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    suspend fun image(@PathVariable id: String): ByteArray = fileService.readFile(
        FILE_DIR,
        id
    ).awaitSingle()
}