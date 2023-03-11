package com.hitkey.file.controller

import com.hitkey.common.config.ParamIsRequired
import com.hitkey.common.data.UserDTO
import com.hitkey.file.controller.rest.FileRequest
import com.hitkey.file.service.FileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("image")
class ImageController {
    companion object {
        private const val FILE_DIR = "image"
    }

    @Autowired
    private lateinit var fileService: FileService

    @PostMapping("save")
    fun saveImage(@RequestBody payload: FileRequest) = Mono
        .create {
            if (payload.file.isBlank())
                it.error(ParamIsRequired("file param in 64 require"))
            else
                it.success("")
        }
        .map { Base64.getDecoder().decode(payload.file) }
        .publish { fileService.saveFile(user.id, it, FILE_DIR) }

    @GetMapping("{id}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun image(@PathVariable id: String) = fileService.readFile(
        FILE_DIR,
        id
    )

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: String) = fileService.removeFile(
        user.id,
        FILE_DIR,
        id
    )

    private val user
        get() = SecurityContextHolder
            .getContext()
            .authentication
            .principal as UserDTO
}