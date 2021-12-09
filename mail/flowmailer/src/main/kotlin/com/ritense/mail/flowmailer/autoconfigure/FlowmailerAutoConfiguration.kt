/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.mail.flowmailer.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.mail.MailDispatcher
import com.ritense.mail.flowmailer.config.FlowmailerProperties
import com.ritense.mail.flowmailer.service.FlowmailerMailDispatcher
import com.ritense.mail.flowmailer.service.FlowmailerTokenService
import com.ritense.valtimo.contract.json.Mapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(value = [FlowmailerProperties::class])
class FlowmailerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RestTemplate::class)
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder().build()
    }

    @Bean
    @ConditionalOnMissingBean(MailDispatcher::class)
    fun mailDispatcher(
        flowmailerProperties: FlowmailerProperties,
        flowmailerTokenService: FlowmailerTokenService,
        restTemplate: RestTemplate,
        objectMapper: ObjectMapper = Mapper.INSTANCE.get()
    ): MailDispatcher {
        return FlowmailerMailDispatcher(flowmailerProperties, flowmailerTokenService, restTemplate, objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean(FlowmailerTokenService::class)
    fun flowmailerTokenService(
        flowmailerProperties: FlowmailerProperties,
        restTemplate: RestTemplate
    ): FlowmailerTokenService {
        return FlowmailerTokenService(flowmailerProperties, restTemplate)
    }
}