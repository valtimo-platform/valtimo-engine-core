/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.objectmanagement.autoconfigure

import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objectmanagement.security.config.ObjectManagementHttpSecurityConfigurer
import com.ritense.objectmanagement.service.ObjectManagementProviderImpl
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.objectmanagement.web.rest.ObjectManagementResource
import com.ritense.plugin.service.PluginService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.objectmanagement.repository"])
@EntityScan("com.ritense.objectmanagement.domain")
class ObjectManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectManagementService::class)
    fun objectManagementService(
        objectManagementRepository: ObjectManagementRepository,
        pluginService: PluginService
    ): ObjectManagementService {
        return ObjectManagementService(
            objectManagementRepository,
            pluginService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ObjectManagementProviderImpl::class)
    fun objectManagementProvider(objectManagementService: ObjectManagementService): ObjectManagementProviderImpl {
        return ObjectManagementProviderImpl(
            objectManagementService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ObjectManagementResource::class)
    fun objectManagementResource(
        objectManagementService: ObjectManagementService
    ): ObjectManagementResource {
        return ObjectManagementResource(
            objectManagementService
        )
    }

    @Order(301)
    @Bean
    @ConditionalOnMissingBean(ObjectManagementHttpSecurityConfigurer::class)
    fun objectManagementHttpSecurityConfigurer(): ObjectManagementHttpSecurityConfigurer {
        return ObjectManagementHttpSecurityConfigurer()
    }

}
