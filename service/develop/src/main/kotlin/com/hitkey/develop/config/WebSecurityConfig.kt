package com.hitkey.develop.config

import com.hitkey.develop.component.AuthenticationManager
import com.hitkey.develop.component.SecurityContextRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfig {
    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var securityContextRepository: SecurityContextRepository

    @Bean
    fun securityWebFilterChain(httpSecurity: ServerHttpSecurity): SecurityWebFilterChain = httpSecurity
        .exceptionHandling()
        .authenticationEntryPoint { swe, _ ->
            Mono.fromRunnable {
                swe.response.statusCode = HttpStatus.UNAUTHORIZED
            }
        }
        .accessDeniedHandler { swe, _ ->
            Mono.fromRunnable {
                swe.response.statusCode = HttpStatus.FORBIDDEN
            }
        }
        .and()
        .csrf().disable()
        .formLogin().disable()
        .httpBasic().disable()
        .authenticationManager(authenticationManager)
        .securityContextRepository(securityContextRepository)
        .authorizeExchange()
        .pathMatchers("/app/*").permitAll()
        .anyExchange().authenticated()
        .and()
        .build()
}