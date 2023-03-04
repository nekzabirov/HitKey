package com.hitkey.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.LocalDate

@Configuration
class WebFluxConfig {

    @Bean
    fun objectMapperBuilder(): Jackson2ObjectMapperBuilder {
        val builder = Jackson2ObjectMapperBuilder()
        builder.modules(JavaTimeModule())
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        builder.serializerByType(LocalDate::class.java, ToStringSerializer.instance)
        return builder
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return objectMapperBuilder()
            .build<ObjectMapper>().apply {
                registerModule(JavaTimeModule())
            }
    }
}
