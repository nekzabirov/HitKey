package com.hitkey.file.service

import com.hitkey.common.config.NotFoundException
import com.hitkey.common.config.ParamIsRequired
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.File
import java.time.LocalDate
import java.util.Base64
import java.util.Date

@Service
class FileService {
    private val rootFileDir = File("/app/files")

    fun saveFile(buffer: Mono<ByteArray>, directory: String) = buffer.flatMap {
        saveFile(it, directory)
    }

    fun saveFile(buffer: ByteArray, directory: String) = createDirectory(directory)
        .map { Date().toString() }
        .map { it: String ->
            val s: String = Base64.getEncoder().encodeToString(it.toByteArray())
            File(rootFileDir, "$directory/$s")
        }
        .publishOn(Schedulers.boundedElastic())
        .handle { t, u ->
            if (t.exists()) {
                u.error(ParamIsRequired("File name error"))
                return@handle
            }

            t.createNewFile()

            t.outputStream().write(buffer)

            u.next(t.name)
        }

    fun readFile(directory: String, name: String) = Mono.create {
        val file = File(rootFileDir, "$directory/$name")

        if (!file.exists())
            it.error(NotFoundException())
        else
            it.success(file)
    }.map {
        it.readBytes()
    }

    fun createDirectory(name: String) = Mono.create {
        val dir = File(rootFileDir, name)

        if (!dir.exists())
            dir.mkdir()

        it.success(true)
    }

}