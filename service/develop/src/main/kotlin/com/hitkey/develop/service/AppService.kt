package com.hitkey.develop.service

import com.hitkey.common.component.HitCrypto
import com.hitkey.common.config.NotFoundException
import com.hitkey.develop.data.AppDTOMapper
import com.hitkey.develop.database.entity.ApplicationEntity
import com.hitkey.develop.database.repo.ApplicationRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AppService {
    @Autowired
    private lateinit var applicationRepo: ApplicationRepo

    @Autowired
    private lateinit var crypto: HitCrypto

    @Autowired
    private lateinit var appDTOMapper: AppDTOMapper

    fun create(ownerID: Long, name: String, logoID: String) = Mono
        .just(
            ApplicationEntity(
                name = name,
                logoID = logoID,
                ownerID = ownerID,
                token = crypto.generateAuthToken(System.currentTimeMillis())
            )
        )
        .flatMap { applicationRepo.save(it) }

    fun update(ownerID: Long, appToken: String, name: String?, logoID: String?) = applicationRepo
        .findByToken(appToken)
        .switchIfEmpty(Mono.error(NotFoundException()))
        .handle { t, u ->
            if (t.ownerID != ownerID)
                u.error(NotFoundException())
            else
                u.next(t)
        }
        .map {
            it.apply {
                if (name != null)
                    this.name = name
                if (logoID != null)
                    this.logoID = logoID
            }
        }
        .flatMap { applicationRepo.save(it) }

    fun delete(ownerID: Long, appToken: String) = applicationRepo
        .findByToken(appToken)
        .switchIfEmpty(Mono.error(NotFoundException()))
        .handle { t, u ->
            if (t.ownerID != ownerID)
                u.error(NotFoundException())
            else
                u.next(t)
        }
        .flatMap { applicationRepo.delete(it) }

    fun listOf(ownerID: Long) = applicationRepo.findAllByOwnerID(ownerID)
        .map(appDTOMapper)

    fun infoBy(token: String) = applicationRepo.findByToken(token)
        .map(appDTOMapper)
}