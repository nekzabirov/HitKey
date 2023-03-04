package com.hitkey.develop.database.repo

import com.hitkey.develop.database.entity.ApplicationEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ApplicationRepo: ReactiveCrudRepository<ApplicationEntity, Long> {
    fun findByToken(token: String): Mono<ApplicationEntity>

    fun findAllByOwnerID(ownerID: Long): Flux<ApplicationEntity>
}