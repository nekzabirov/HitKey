package com.hitkey.develop.service

import com.hitkey.common.data.HitResponse
import com.hitkey.common.config.NotFoundException
import com.netflix.discovery.EurekaClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class FileService(private val webClientBuilder: WebClient.Builder) {

    @Autowired
    private lateinit var eurekaClient: EurekaClient

    private val webClient
        get() = webClientBuilder
            .baseUrl("http://FILE/api/v1/")
            .build()

    fun addUserFile(file: String) = webClient.post()
        .uri("user/image/save")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(FileRequest(file))
        .retrieve()
        .bodyToMono(HitResponse.OK::class.java)

    fun findUserFile(fileID: String) = webClient.get()
        .uri("user/image/$fileID")
        .retrieve()
        .onStatus(
            { status -> status.value() == HttpStatus.NOT_FOUND.value() },
            { _ -> Mono.error(NotFoundException()) }
        )
        .bodyToMono<ByteArray>()

    data class FileRequest(
        val file: String
    )
}