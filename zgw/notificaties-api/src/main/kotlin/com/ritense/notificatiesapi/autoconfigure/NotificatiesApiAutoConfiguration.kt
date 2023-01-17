/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.notificatiesapi.autoconfigure

import com.ritense.notificatiesapi.NotificatiesApiPluginFactory
import com.ritense.notificatiesapi.client.NotificatiesApiClient
import com.ritense.notificatiesapi.repository.NotificatiesApiAbonnementLinkRepository
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.service.PluginService
import io.netty.handler.logging.LogLevel
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.notificatiesapi.repository"])
@EntityScan("com.ritense.notificatiesapi.domain")
class NotificatiesApiAutoConfiguration {

    @Bean
    fun notificatiesApiClient(webclient: WebClient): NotificatiesApiClient {
        return NotificatiesApiClient(webclient)
    }

    @Bean
    @ConditionalOnMissingBean(NotificatiesApiPluginFactory::class)
    fun notificatiesApiPluginFactory(
        pluginService: PluginService,
        pluginConfigurationRepository: PluginConfigurationRepository,
        client: NotificatiesApiClient,
        abonnementLinkRepository: NotificatiesApiAbonnementLinkRepository
    ): NotificatiesApiPluginFactory {
        return NotificatiesApiPluginFactory(
            pluginService,
            client,
            abonnementLinkRepository
        )
    }

    @Bean
    @ConditionalOnMissingBean(WebClient::class)
    fun notificatiesApiWebClient(): WebClient {
        return WebClient.builder().clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create().wiretap(
                    "reactor.netty.http.client.HttpClient",
                    LogLevel.DEBUG,
                    AdvancedByteBufFormat.TEXTUAL
                )
            )
        ).build()
    }
}