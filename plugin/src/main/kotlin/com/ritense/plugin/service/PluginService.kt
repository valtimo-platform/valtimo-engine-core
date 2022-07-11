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

package com.ritense.plugin.service

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.exception.PluginPropertyParseException
import com.ritense.plugin.exception.PluginPropertyRequiredException
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.web.rest.dto.PluginActionDefinitionDto
import com.ritense.valtimo.contract.json.Mapper

class PluginService(
    private var pluginDefinitionRepository: PluginDefinitionRepository,
    private var pluginConfigurationRepository: PluginConfigurationRepository,
    private var pluginActionDefinitionRepository: PluginActionDefinitionRepository,
    private var pluginFactories: List<PluginFactory<*>>
) {

    fun getPluginDefinitions(): List<PluginDefinition> {
        return pluginDefinitionRepository.findAll()
    }

    fun getPluginConfigurations(): List<PluginConfiguration> {
        return pluginConfigurationRepository.findAll()
    }

    fun getPluginConfiguration(key: String): PluginConfiguration {
        return pluginConfigurationRepository.getById(key)
    }

    fun createPluginConfiguration(
        title: String,
        properties: JsonNode,
        pluginDefinitionKey: String
    ): PluginConfiguration {
        val pluginDefinition = pluginDefinitionRepository.getById(pluginDefinitionKey)
        validateProperties(properties, pluginDefinition)

        return pluginConfigurationRepository.save(
            PluginConfiguration(PluginConfigurationId.newId(), title, properties, pluginDefinition)
        )
    }

    fun getPluginDefinitionActions(
        pluginDefinitionKey: String,
        activityType: ActivityType?
    ): List<PluginActionDefinitionDto> {
        val actions = if (activityType == null)
            pluginActionDefinitionRepository.findByIdPluginDefinitionKey(pluginDefinitionKey)
        else
            pluginActionDefinitionRepository.findByIdPluginDefinitionKeyAndActivityTypes(pluginDefinitionKey, activityType)

        return actions.map {
            PluginActionDefinitionDto(
                it.id.key,
                it.title,
                it.description
            )
        }
    }

    // TODO: Replace this with action invocation method
    fun createPluginInstance(configurationKey: String): Any {
        val configuration = getPluginConfiguration(configurationKey)
        val pluginFactory = pluginFactories.filter {
            it.canCreate(configuration)
        }.firstOrNull()
        return pluginFactory!!.create(configuration)!!
    }

    private fun validateProperties(properties: JsonNode, pluginDefinition: PluginDefinition) {
        assert(properties.isObject)

        pluginDefinition.pluginProperties.forEach { pluginProperty ->
            val propertyNode = properties[pluginProperty.fieldName]

            if (propertyNode == null || propertyNode.isMissingNode || propertyNode.isNull) {
                if (pluginProperty.required) {
                    throw PluginPropertyRequiredException(pluginProperty.fieldName, pluginDefinition.title)
                }
            } else {
                try {
                    val propertyClass = Class.forName(pluginProperty.fieldType)
                    val property = Mapper.INSTANCE.get().treeToValue(propertyNode, propertyClass)
                    assert(property != null)
                } catch (e: Exception) {
                    throw PluginPropertyParseException(pluginProperty.fieldName, pluginDefinition.title, e)
                }
            }
        }
    }
}
