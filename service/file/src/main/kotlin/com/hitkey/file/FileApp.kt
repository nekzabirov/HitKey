package com.hitkey.file

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication(scanBasePackages = ["com.hitkey"])
@EnableDiscoveryClient
class FileServiceApplication

fun main(args: Array<String>) {
    runApplication<FileServiceApplication>(*args)
}
