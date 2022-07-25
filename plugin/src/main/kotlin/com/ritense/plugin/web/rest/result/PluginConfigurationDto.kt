package com.ritense.plugin.web.rest.result

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginDefinition

class PluginConfigurationDto(
    pluginConfiguration: PluginConfiguration
) {
    val id: String
    val title: String
    var properties: JsonNode? = null
    val pluginDefinition: PluginDefinition

    init {
        id = pluginConfiguration.id.id.toString()
        title = pluginConfiguration.title
        pluginDefinition = pluginConfiguration.pluginDefinition

        if (pluginConfiguration.properties != null) {
            val configurationProperties: ObjectNode = pluginConfiguration.properties!!.deepCopy()

            var secretDefinitionProperties = pluginConfiguration.pluginDefinition.pluginProperties.filter {
                it.secret
            }.map {
                it.fieldName
            }

            configurationProperties.remove(secretDefinitionProperties)

            properties = configurationProperties
        }

    }
}