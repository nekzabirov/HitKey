package com.hitkey.file.service

import com.hitkey.common.config.NotFoundException
import com.hitkey.common.config.NotPermitted
import com.hitkey.common.config.ParamIsRequired
import com.hitkey.common.data.UserDTO
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.attribute.UserDefinedFileAttributeView
import java.util.*


@Service
class FileService {
    private val rootFileDir = File("/app/files")

    fun saveFile(userID: Long, buffer: Mono<ByteArray>, directory: String) = buffer.flatMap {
        saveFile(userID, it, directory)
    }

    fun saveFile(userID: Long, buffer: ByteArray, directory: String) = createDirectory(directory)
        .map { Date().toString() }
        .map { it: String ->
            val s: String = Base64.getEncoder().encodeToString(it.toByteArray())
            File(rootFileDir, "$directory/${s}.$userID")
        }
        .publishOn(Schedulers.boundedElastic())
        .handle { t, u ->
            if (t.exists()) {
                u.error(ParamIsRequired("File name error"))
                return@handle
            }

            t.createNewFile()

            t.outputStream().write(buffer)

            /*val view: UserDefinedFileAttributeView = Files.getFileAttributeView(
                t.toPath(),
                UserDefinedFileAttributeView::class.java
            )

            view.write("user.id", StandardCharsets.UTF_8.encode(userID.toString()));*/

            u.next(t.name)
        }

    fun readFile(directory: String, name: String) = Mono
        .create {
            val file = File(rootFileDir, "$directory/$name")

            if (!file.exists())
                it.error(NotFoundException())
            else
                it.success(file)
        }
        .map {
            it.readBytes()
        }

    fun removeFile(userID: Long, directory: String, name: String) = Mono
        .create {
            val file = File(rootFileDir, "$directory/$name")

            if (!file.exists())
                it.error(NotFoundException())
            else
                it.success(file)
        }
        .handle { file, u ->
            /*val view = Files.getFileAttributeView(
                file.toPath(),
                UserDefinedFileAttributeView::class.java
            )

            if (view.readUserID().toLong() != userID) {
                u.error(NotPermitted())
                return@handle
            }*/

            if (file.extension.toLong() != userID) {
                u.error(NotPermitted())
                return@handle
            }

            file.delete()

            u.next(true)
        }

    private fun createDirectory(name: String) = Mono.create {
        val dir = File(rootFileDir, name)

        if (!dir.exists())
            dir.mkdir()

        it.success(true)
    }

    @Throws(Exception::class)
    private fun UserDefinedFileAttributeView.readUserID(): String {
        val buffer: ByteBuffer = ByteBuffer.allocate(this.size("user.id"))
        this.read("user.id", buffer)
        buffer.flip()
        return StandardCharsets.UTF_8.decode(buffer).toString()
    }

}