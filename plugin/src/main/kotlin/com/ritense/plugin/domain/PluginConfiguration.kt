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

package com.ritense.plugin.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.plugin.service.PluginConfigurationEntityListener
import com.ritense.valtimo.contract.json.Mapper
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@EntityListeners(PluginConfigurationEntityListener::class)
@Table(name = "plugin_configuration")
class PluginConfiguration(
    @Id
    @Embedded
    val id: PluginConfigurationId,
    @Column(name = "title")
    var title: String,
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "properties", columnDefinition = "JSON")
    val properties: ObjectNode? = null,
    @JoinColumn(name = "plugin_definition_key", updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    val pluginDefinition: PluginDefinition,
) {
    inline fun <reified T> getProperties(): T {
        return if (properties == null) {
            throw IllegalStateException("No properties found for plugin configuration $title (${id.id})")
        } else {
            Mapper.INSTANCE.get().treeToValue(properties, T::class.java)
        }
    }

    fun updateProperties(propertiesForUpdate: ObjectNode) {
        pluginDefinition.pluginProperties.forEach {
            val updateValue = propertiesForUpdate.get(it.fieldName)
            if (!it.secret || !nodeIsEmpty(updateValue)) {
                properties?.replace(it.fieldName, updateValue)
            }
        }
    }

    private fun nodeIsEmpty(node: JsonNode?): Boolean {
        return node == null || node.isNull ||
            (node is TextNode && node.textValue() == "")
    }
}
