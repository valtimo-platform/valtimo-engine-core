/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.autoconfigure

import com.ritense.openzaak.plugin.OpenZaakPluginFactory
import com.ritense.openzaak.plugin.token.OpenZaakPluginTokenGeneratorService
import com.ritense.openzaak.plugin.token.ValtimoOpenZaakPluginTokenGeneratorService
import com.ritense.plugin.service.PluginService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class OpenZaakPluginAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OpenZaakPluginTokenGeneratorService::class)
    fun openZaakPluginTokenGeneratorService(): OpenZaakPluginTokenGeneratorService = ValtimoOpenZaakPluginTokenGeneratorService()

    @Bean
    fun openZaakPluginFactory(
        pluginService: PluginService,
        tokenGeneratorService: OpenZaakPluginTokenGeneratorService
    ): OpenZaakPluginFactory {
        return OpenZaakPluginFactory(pluginService, tokenGeneratorService)
    }
}