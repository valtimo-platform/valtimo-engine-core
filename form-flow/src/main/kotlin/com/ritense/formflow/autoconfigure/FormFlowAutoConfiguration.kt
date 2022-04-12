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

package com.ritense.formflow.autoconfigure

import com.ritense.formflow.expression.ExpressionProcessorFactory
import com.ritense.formflow.expression.spel.SpelExpressionProcessorFactory
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository
import com.ritense.formflow.repository.FormFlowStepInstanceRepository
import com.ritense.formflow.repository.FormFlowStepRepository
import com.ritense.formflow.service.FormFlowDeploymentService
import com.ritense.formflow.service.FormFlowService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackageClasses = [
    FormFlowDefinitionRepository::class,
    FormFlowStepRepository::class,
    FormFlowInstanceRepository::class,
    FormFlowStepInstanceRepository::class])
@EntityScan(basePackages = ["com.ritense.formflow.domain"])
class FormFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ExpressionProcessorFactory::class)
    fun expressionProcessorFactory(): ExpressionProcessorFactory {
        return SpelExpressionProcessorFactory()
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowService::class)
    fun formFlowService(
        formFlowDefinitionRepository: FormFlowDefinitionRepository,
        formFlowInstanceRepository: FormFlowInstanceRepository,
        expressionProcessorFactory: ExpressionProcessorFactory
    ): FormFlowService {
        return FormFlowService(formFlowDefinitionRepository, formFlowInstanceRepository, expressionProcessorFactory)
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowDeploymentService::class)
    fun formFlowDeploymentService(
        resourceLoader: ResourceLoader,
        formFlowService: FormFlowService
    ): FormFlowDeploymentService {
        return FormFlowDeploymentService(resourceLoader, formFlowService)
    }
}