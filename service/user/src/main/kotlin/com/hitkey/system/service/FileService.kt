package com.hitkey.system.service

import com.hitkey.common.component.HitCrypto
import com.hitkey.common.data.HitResponse
import com.hitkey.common.config.NotFoundException
import com.hitkey.system.database.entity.user.UserEntity
import com.netflix.discovery.EurekaClient
import org.apache.commons.codec.digest.Crypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class FileService(private val webClientBuilder: WebClient.Builder) {
    private data class FileRequest(
        val file: String
    )

    @Autowired
    private lateinit var crypt: HitCrypto

    private val webClient
        get() = webClientBuilder
            .baseUrl("http://FILE/api/v1/")
            .build()

    fun saveImage(userID: Long, fileBase64: String) = webClient.post()
        .uri("image/save")
        .contentType(MediaType.APPLICATION_JSON)
        .headers {
            it[HttpHeaders.AUTHORIZATION] = "Bearer ${crypt.generateAuthToken(userID)}"
        }
        .bodyValue(FileRequest(fileBase64))
        .retrieve()
        .bodyToMono<HitResponse.OK<String>>()
        .mapNotNull { it.data }

    fun removeImage(userID: Long, fileID: String) = webClient.delete()
        .uri("image/$fileID")
        .headers {
            it[HttpHeaders.AUTHORIZATION] = "Bearer ${crypt.generateAuthToken(userID)}"
        }
        .retrieve()
        .bodyToMono<HitResponse.OK<Boolean>>()
        .mapNotNull { it.data }
}