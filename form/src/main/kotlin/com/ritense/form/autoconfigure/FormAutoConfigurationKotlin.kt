/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.form.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.autodeployment.FormDefinitionDeploymentService
import com.ritense.form.repository.IntermediateSubmissionRepository
import com.ritense.form.security.config.FormHttpSecurityConfigurerKotlin
import com.ritense.form.service.FormDefinitionExporter
import com.ritense.form.service.FormDefinitionImporter
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.service.FormSubmissionService
import com.ritense.form.service.FormSupportedProcessLinksHandler
import com.ritense.form.service.IntermediateSubmissionService
import com.ritense.form.service.PrefillFormService
import com.ritense.form.service.impl.DefaultFormSubmissionService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.form.web.rest.FormResource
import com.ritense.form.web.rest.IntermediateSubmissionResource
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
class FormAutoConfigurationKotlin {

    @Bean
    @ConditionalOnMissingBean(FormSupportedProcessLinksHandler::class)
    fun formSupportedProcessLinks(
        formDefinitionService: FormDefinitionService
    ) = FormSupportedProcessLinksHandler(
        formDefinitionService
    )

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(FormHttpSecurityConfigurerKotlin::class)
    fun formHttpSecurityConfigurerKotlin() = FormHttpSecurityConfigurerKotlin()

    @Bean
    @ConditionalOnMissingBean(FormResource::class)
    fun formResource(
        formSubmissionService: FormSubmissionService,
        prefillFormService: PrefillFormService,
        formDefinitionService: FormDefinitionService,
    ) = FormResource(
        formSubmissionService,
        prefillFormService,
        formDefinitionService
    )

    @Bean
    @ConditionalOnMissingBean
    fun formDefinitionImporter(
        formDefinitionDeploymentService: FormDefinitionDeploymentService
    ): FormDefinitionImporter = FormDefinitionImporter(formDefinitionDeploymentService)

    @Bean
    @ConditionalOnMissingBean(FormSubmissionService::class)
    fun formSubmissionService(
        processLinkService: ProcessLinkService,
        formDefinitionService: FormIoFormDefinitionService,
        documentService: JsonSchemaDocumentService,
        processDocumentAssociationService: ProcessDocumentAssociationService,
        processDocumentService: ProcessDocumentService,
        camundaTaskService: CamundaTaskService,
        repositoryService: CamundaRepositoryService,
        applicationEventPublisher: ApplicationEventPublisher,
        prefillFormService: PrefillFormService,
        authorizationService: AuthorizationService,
        valueResolverService: ValueResolverService,
        objectMapper: ObjectMapper,
    ) = DefaultFormSubmissionService(
        processLinkService,
        formDefinitionService,
        documentService,
        processDocumentAssociationService,
        processDocumentService,
        camundaTaskService,
        repositoryService,
        applicationEventPublisher,
        prefillFormService,
        authorizationService,
        valueResolverService,
        objectMapper,
    )

    @Bean
    @ConditionalOnMissingBean(FormDefinitionExporter::class)
    fun formDefinitionExporter(
        objectMapper: ObjectMapper,
        formDefinitionService: FormDefinitionService
    ) = FormDefinitionExporter(
        objectMapper,
        formDefinitionService
    )

    @Bean
    @ConditionalOnMissingBean(IntermediateSubmissionService::class)
    fun intermediateSubmissionService(
        intermediateSubmissionRepository: IntermediateSubmissionRepository
    ) = IntermediateSubmissionService(
        intermediateSubmissionRepository
    )

    @Bean
    @ConditionalOnMissingBean(IntermediateSubmissionResource::class)
    fun intermediateSubmissionResource(
        intermediateSubmissionService: IntermediateSubmissionService
    ) = IntermediateSubmissionResource(
        intermediateSubmissionService
    )
}