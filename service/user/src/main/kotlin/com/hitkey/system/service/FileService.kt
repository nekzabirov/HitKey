package com.hitkey.system.service

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
class FileService {

    @Autowired
    private lateinit var eurekaClient: EurekaClient

    private val webClient: WebClient
        get() {
            val instance = eurekaClient.getApplication("file_service").instances.run {
                if (this.isEmpty())
                    throw RuntimeException("file service isn't run")

                random()
            }

            return WebClient.create(
                 "https://" + instance.hostName + "/api/v1/"
            )
        }

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