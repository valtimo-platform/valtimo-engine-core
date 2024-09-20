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

package com.ritense.objectenapi

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.client.ObjectenApiClient
import com.ritense.objectenapi.client.ObjectsList
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.valtimo.contract.validation.Url
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import java.net.URI

@Plugin(
    key = "objectenapi",
    title = "Objecten API",
    description = "Connects to the Objecten API"
)
class ObjectenApiPlugin(
    private val objectenApiClient: ObjectenApiClient
) {
    @Url
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: ObjectenApiAuthentication

    @PluginAction(
        key = "delete-object",
        title = "Delete object",
        description = "Delete an object from the Objecten API",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun deleteObject(@PluginActionProperty objectUrl: URI): HttpStatus {
        if (!objectUrl.toASCIIString().startsWith(url.toASCIIString())) {
            throw IllegalStateException("Failed to delete object with url '$objectUrl'. Object isn't part of Objecten API with url '$url'.")
        }

        return objectenApiClient.deleteObject(authenticationPluginConfiguration, objectUrl)
    }

    fun getObject(objectUrl: URI): ObjectWrapper {
        return objectenApiClient.getObject(authenticationPluginConfiguration, objectUrl)
    }

    fun getObjectsByObjectTypeId(
        objecttypesApiUrl: URI,
        objectsApiUrl: URI,
        objecttypeId: String,
        pageable: Pageable
    ): ObjectsList {
        return objectenApiClient.getObjectsByObjecttypeUrl(
            authenticationPluginConfiguration,
            objecttypesApiUrl,
            objectsApiUrl,
            objecttypeId,
            pageable
        )
    }

    fun getObjectsByObjectTypeIdWithSearchParams(
        objecttypesApiUrl: URI,
        objecttypeId: String,
        searchString: String,
        pageable: Pageable
    ): ObjectsList {
        return objectenApiClient.getObjectsByObjecttypeUrlWithSearchParams(
            authentication = authenticationPluginConfiguration,
            objecttypesApiUrl = objecttypesApiUrl,
            objectsApiUrl = url,
            objectypeId = objecttypeId,
            searchString = searchString,
            pageable = pageable
        )
    }

    fun objectUpdate(objectUrl: URI, objectRequest: ObjectRequest): ObjectWrapper {
        return objectenApiClient.objectUpdate(authenticationPluginConfiguration, objectUrl, objectRequest)
    }

    fun objectPatch(objectUrl: URI, objectRequest: ObjectRequest): ObjectWrapper {
        return objectenApiClient.objectPatch(authenticationPluginConfiguration, objectUrl, objectRequest)
    }

    fun createObject(objectRequest: ObjectRequest): ObjectWrapper {
        return objectenApiClient.createObject(authenticationPluginConfiguration, url, objectRequest)
    }

    companion object {
        const val URL_PROPERTY = "url"

        fun findConfigurationByUrl(url: URI) =
            { properties: JsonNode -> url.toString().startsWith(properties.get(URL_PROPERTY).textValue()) }
    }
}