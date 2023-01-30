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

package com.ritense.verzoek

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.service.ProcessDocumentService
import java.net.URI
import org.springframework.context.event.EventListener

class VerzoekPluginEventListener(
    private val pluginService: PluginService,
    private val objectManagementService: ObjectManagementService,
    private val documentService: DocumentService,
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val processDocumentService: ProcessDocumentService
) {

    @EventListener(NotificatiesApiNotificationReceivedEvent::class)
    fun createZaakFromNotificatie(event: NotificatiesApiNotificationReceivedEvent) {
        val objectType = event.kenmerken["objectType"]

        if (!event.kanaal.equals("objecten", ignoreCase = true) ||
            !event.actie.equals("create", ignoreCase = true) ||
            objectType == null
        ) {

            return
        }

        val objectManagement = objectManagementService.findByObjectTypeId(objectType.substringAfterLast("/")) ?: return

        pluginService.createInstance(VerzoekPlugin::class.java) { properties: JsonNode ->
            properties.get("objectManagementId").textValue().equals(objectManagement.id.toString())
        }?.run {
            val objectenApiPlugin =
                pluginService.createInstance(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
            val verzoekObjectData = objectenApiPlugin.getObject(URI(event.resourceUrl)).record.data
                ?: throw RuntimeException("Verzoek meta data was empty!")

            val verzoekType = verzoekObjectData.get("type")?.textValue()
            val verzoekTypeProperties = verzoekProperties.firstOrNull { props -> props.type.equals(verzoekType, true) }
                ?: throw RuntimeException("Could not find properties of type $verzoekType")
            val document = documentService.createDocument(
                NewDocumentRequest(
                    verzoekTypeProperties.caseDefinitionName,
                    verzoekObjectData.get("data") ?: throw RuntimeException("Verzoek Object data was empty!")
                )
            ).also { documentResult ->
                if (documentResult == null || documentResult.errors().size > 0) {
                    throw RuntimeException(
                        "Could not create document for case ${verzoekTypeProperties.caseDefinitionName}\nReason:\n"
                                + documentResult.errors().map {
                            it.asString()
                        }.joinToString {
                            "\n - $it"
                        })
                }
            }.resultingDocument().orElseThrow()

            val initiatorType = if(verzoekObjectData.has("kvk")) { "kvk" } else { "bsn" }

            val zaakTypeUrl = zaakTypeLinkService.findBy(document.definitionId().name()).zaakTypeUrl
            val startProcessRequest = StartProcessForDocumentRequest(
                document.id(), systemProcessDefinitionKey, mapOf(
                    "RSIN" to this.rsin.toString(),
                    "zaakTypeUrl" to zaakTypeUrl.toString(),
                    "rolTypeUrl" to verzoekTypeProperties.initiatorRoltypeUrl.toString(),
                    "verzoekObjectUrl" to event.resourceUrl,
                    "initiatorType" to initiatorType,
                    "initiatorValue" to verzoekObjectData.get(initiatorType).textValue()
                )
            )

            val result = processDocumentService.startProcessForDocument(startProcessRequest)
            if (result == null || result.errors().size > 0) {
                throw RuntimeException(
                    "Could not start process ${startProcessRequest.processDefinitionKey}\nReason:\n"
                            + result.errors().map {
                        it.asString()
                    }.joinToString {
                        "\n - $it"
                    })
            }
        }

    }
}