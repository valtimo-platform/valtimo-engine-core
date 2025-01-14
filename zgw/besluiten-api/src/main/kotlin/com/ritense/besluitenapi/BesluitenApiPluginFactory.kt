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

package com.ritense.besluitenapi

import com.ritense.besluitenapi.client.BesluitenApiClient
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZaakUrlProvider

class BesluitenApiPluginFactory(
    pluginService: PluginService,
    private val besluitenApiClient: BesluitenApiClient,
    private val urlProvider: ZaakUrlProvider,
) : PluginFactory<BesluitenApiPlugin>(pluginService) {
    override fun create(): BesluitenApiPlugin {
        return BesluitenApiPlugin(
            besluitenApiClient,
            urlProvider
        )
    }
}