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

package com.ritense.documentenapi.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.document.domain.RelatedFile
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.Companion.VIEW
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.client.PatchDocumentRequest
import com.ritense.documentenapi.web.rest.dto.ModifyDocumentRequest
import com.ritense.documentenapi.web.rest.dto.RelatedFileDto
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.processlink.service.PluginProcessLinkService
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.net.URI
import java.util.UUID

@Transactional
class DocumentenApiService(
    private val pluginService: PluginService,
    private val catalogiService: CatalogiService,
    private val authorizationService: AuthorizationService,
    private val documentDefinitionService: JsonSchemaDocumentDefinitionService,
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
    private val pluginProcessLinkService: PluginProcessLinkService,
    private val camundaRepositoryService: CamundaRepositoryService,
) {
    fun downloadInformatieObject(pluginConfigurationId: String, documentId: String): InputStream {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        return documentApiPlugin.downloadInformatieObject(documentId)
    }

    fun getInformatieObject(pluginConfigurationId: String, documentId: String): DocumentInformatieObject {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        return documentApiPlugin.getInformatieObject(documentId)
    }

    fun modifyInformatieObject(
        pluginConfigurationId: String,
        documentId: String,
        modifyDocumentRequest: ModifyDocumentRequest
    ): RelatedFile? {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        val documentUrl = documentApiPlugin.createInformatieObjectUrl(documentId)
        val informatieObject =
            documentApiPlugin.modifyInformatieObject(documentUrl, PatchDocumentRequest(modifyDocumentRequest))
        return getRelatedFiles(informatieObject, pluginConfigurationId)
    }

    fun deleteInformatieObject(pluginConfigurationId: String, documentId: String) {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        val documentUrl = documentApiPlugin.createInformatieObjectUrl(documentId)
        documentApiPlugin.deleteInformatieObject(documentUrl)
    }

    fun getApiVersions(caseDefinitionName: String): List<String> {
        documentDefinitionService.requirePermission(caseDefinitionName, VIEW)
        val link =
            documentDefinitionProcessLinkService.getDocumentDefinitionProcessLink(caseDefinitionName, "DOCUMENT_UPLOAD")
        if (link.isEmpty) {
            return emptyList()
        }
        val processDefinitionKey = link.get().id.processDefinitionKey
        val detectedVersions = runWithoutAuthorization {
            camundaRepositoryService.findLinkedProcessDefinitions(byKey(processDefinitionKey).and(byLatestVersion()))
                .asSequence()
                .flatMap { pluginProcessLinkService.getProcessLinks(it.id) }
                .map { pluginService.getPluginConfiguration(it.pluginConfigurationId) }
                .filter { it.pluginDefinition.key == DocumentenApiPlugin.PLUGIN_KEY }
                .mapNotNull { (pluginService.createInstance(it) as DocumentenApiPlugin).apiVersion }
                .toList()
        }
        return detectedVersions.sorted()
    }

    private fun getRelatedFiles(
        informatieObject: DocumentInformatieObject,
        pluginConfigurationId: String
    ): RelatedFileDto {
        return RelatedFileDto(
            fileId = UUID.fromString(informatieObject.url.path.substringAfterLast("/")),
            fileName = informatieObject.bestandsnaam,
            sizeInBytes = informatieObject.bestandsomvang,
            createdOn = informatieObject.creatiedatum.atStartOfDay(),
            createdBy = informatieObject.auteur,
            author = informatieObject.auteur,
            title = informatieObject.titel,
            status = informatieObject.status?.key,
            language = informatieObject.taal,
            pluginConfigurationId = UUID.fromString(pluginConfigurationId),
            identification = informatieObject.identificatie,
            description = informatieObject.beschrijving,
            informatieobjecttype = getInformatieobjecttypeByUri(informatieObject.informatieobjecttype),
            keywords = informatieObject.trefwoorden,
            format = informatieObject.formaat,
            sendDate = informatieObject.verzenddatum,
            receiptDate = informatieObject.ontvangstdatum,
            confidentialityLevel = informatieObject.vertrouwelijkheidaanduiding?.key,
            version = informatieObject.versie,
            indicationUsageRights = informatieObject.indicatieGebruiksrecht
        )
    }

    private fun getInformatieobjecttypeByUri(uri: String?): String? {
        return uri?.let { catalogiService.getInformatieobjecttype(URI(it))?.omschrijving }
    }
}
