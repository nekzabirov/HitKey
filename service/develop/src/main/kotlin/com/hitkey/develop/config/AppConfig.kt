package com.hitkey.develop.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class AppConfig {

    @Bean
    fun restTemplateBuilder(): RestTemplateBuilder {
        return RestTemplateBuilder()
    }

    @Autowired
    private lateinit var loadBalancer: LoadBalancerClient

    @Bean
    @LoadBalanced
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build().apply {
            interceptors.add(LoadBalancerInterceptor(loadBalancer))
        }
    }

    @Bean
    fun restTemplateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { restTemplate ->
            restTemplate.interceptors.add(LoadBalancerInterceptor(loadBalancer))
        }
    }
}