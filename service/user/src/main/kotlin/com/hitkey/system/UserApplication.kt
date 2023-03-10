package com.hitkey.system

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.reactive.config.EnableWebFlux


@SpringBootApplication(scanBasePackages = ["com.hitkey"])
@EnableR2dbcAuditing
@EnableR2dbcRepositories
@EnableDiscoveryClient
@EnableWebFlux
class UserApplication {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun resourceDatabasePopulator(): ResourceDatabasePopulator {
        val populator = ResourceDatabasePopulator()

        PathMatchingResourcePatternResolver()
            .getResources("classpath*:db/migration/V*.sql")
            .sortedBy { it.filename }
            .forEach {
                populator.addScript(it)
            }

        return populator
    }

    @Bean
    fun initializer(
        connectionFactory: ConnectionFactory,
        populator: ResourceDatabasePopulator
    ): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        initializer.setDatabasePopulator(populator)
        return initializer
    }
}

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}
