/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.extension

import com.ritense.extension.listener.BeanExtensionClassRegistrationListener
import com.ritense.extension.listener.ValtimoConfigImportExtensionResourcesListener
import com.ritense.extension.web.rest.ExtensionManagementResource
import com.ritense.extension.web.rest.ExtensionPublicResource
import com.ritense.extension.web.rest.ExtensionSecurityConfigurer
import com.ritense.importer.ImportService
import com.ritense.valtimo.contract.extension.ExtensionClassRegistrationListener
import com.ritense.valtimo.contract.extension.ExtensionResourcesRegistrationListener
import org.pf4j.update.UpdateManager
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.core.io.support.ResourcePatternResolver
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@EnableConfigurationProperties(ExtensionProperties::class)
@AutoConfiguration
class ExtensionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ExtensionManager::class)
    fun valtimoExtensionManager(
        resourceResolver: ResourcePatternResolver,
        environment: Environment,
        extensionProperties: ExtensionProperties,
        ): ExtensionManager {
        val valtimoExtensionPath = if (environment.matchesProfiles("dev")) {
            Path("src/main/resources/config/extensions")
        } else {
            Path("tmp/extensions")
        }
        valtimoExtensionPath.createDirectories()
        return ExtensionManager(
            listOf(valtimoExtensionPath),
            resourceResolver,
            extensionProperties,
        )
    }

    @Bean
    @ConditionalOnMissingBean(UpdateManager::class)
    fun valtimoExtensionUpdateManager(
        resourceResolver: ResourcePatternResolver,
        valtimoExtensionManager: ExtensionManager,
        extensionProperties: ExtensionProperties,
        @Lazy repositories: List<ExtensionUpdateRepository>,
    ): ExtensionUpdateManager {
        return ExtensionUpdateManager(
            valtimoExtensionManager,
            repositories + extensionProperties.getExtensionRepositories(),
        )
    }

    @Bean
    @ConditionalOnMissingBean(ExtensionManagementResource::class)
    fun extensionManagementResource(
        valtimoExtensionManager: ExtensionManager,
        valtimoExtensionUpdateManager: ExtensionUpdateManager,
    ): ExtensionManagementResource {
        return ExtensionManagementResource(
            valtimoExtensionManager,
            valtimoExtensionUpdateManager,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ExtensionPublicResource::class)
    fun extensionPublicResource(
        valtimoExtensionManager: ExtensionManager,
        valtimoExtensionUpdateManager: ExtensionUpdateManager,
    ): ExtensionPublicResource {
        return ExtensionPublicResource(
            valtimoExtensionManager,
            valtimoExtensionUpdateManager,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ValtimoExtensionsInjector::class)
    fun valtimoExtensionsInjector(
        extensionManager: ExtensionManager,
        @Lazy extensionClassRegistrationListeners: List<ExtensionClassRegistrationListener>,
        @Lazy extensionResourcesRegistrationListeners: List<ExtensionResourcesRegistrationListener>,
    ): ValtimoExtensionsInjector {
        return ValtimoExtensionsInjector(
            extensionManager,
            extensionClassRegistrationListeners,
            extensionResourcesRegistrationListeners,
        )
    }

    @Bean
    @ConditionalOnMissingBean(BeanExtensionClassRegistrationListener::class)
    fun beanExtensionClassRegistrationListener(
        extensionManager: ExtensionManager,
        beanFactory: AbstractAutowireCapableBeanFactory,
        extensionProperties: ExtensionProperties,
    ): BeanExtensionClassRegistrationListener {
        return BeanExtensionClassRegistrationListener(
            extensionManager,
            beanFactory,
            extensionProperties,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ValtimoConfigImportExtensionResourcesListener::class)
    fun valtimoConfigImportExtensionResourcesListener(
        importService: ImportService,
    ): ValtimoConfigImportExtensionResourcesListener {
        return ValtimoConfigImportExtensionResourcesListener(
            importService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(MultiInstanceExtensionInstaller::class)
    fun multiInstanceExtensionInstaller(
        extensionManager: ExtensionManager
    ): MultiInstanceExtensionInstaller {
        return MultiInstanceExtensionInstaller(extensionManager)
    }

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(ExtensionSecurityConfigurer::class)
    fun extensionSecurityConfigurer(): ExtensionSecurityConfigurer {
        return ExtensionSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(name = ["locallyPublishedExtensionsRepository"])
    fun locallyPublishedExtensionsRepository(): ExtensionUpdateRepository {
        return ExtensionUpdateRepository(
            "locally-published-extensions-repository",
            Path(System.getProperty("user.home"), ".valtimo_extensions").toUri().toURL()
        )
    }
}