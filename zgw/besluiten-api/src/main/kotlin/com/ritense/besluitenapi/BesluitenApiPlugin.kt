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

package com.ritense.besluitenapi

import com.ritense.besluitenapi.client.BesluitenApiClient
import com.ritense.besluitenapi.domain.BesluitInformatieObject
import com.ritense.besluitenapi.domain.CreateBesluitInformatieObject
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.zgw.Rsin
import mu.KLogger
import mu.KotlinLogging
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

@Plugin(
    key = BesluitenApiPlugin.PLUGIN_KEY,
    title = "Besluiten API",
    description = "Connects to the Besluiten API"
)
class BesluitenApiPlugin(
    val besluitenApiClient: BesluitenApiClient
) {
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "rsin", secret = false)
    lateinit var rsin: Rsin

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: BesluitenApiAuthentication

    @PluginAction(
        key = "link-document-to-besluitt",
        title = "Link Document to besluit",
        description = "Links a document to a besluit",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun linkDocumentToBesluit(
        @PluginActionProperty documentUrl: String,
        @PluginActionProperty besluitUrl: String
    ){
        besluitenApiClient.createBesluitInformatieObject(
            authenticationPluginConfiguration,
            url,
            CreateBesluitInformatieObject(documentUrl, besluitUrl)
        )
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val PLUGIN_KEY = "besluitenapi"
    }
}