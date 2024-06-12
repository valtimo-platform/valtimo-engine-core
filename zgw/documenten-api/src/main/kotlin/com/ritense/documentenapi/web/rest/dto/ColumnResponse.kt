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

package com.ritense.documentenapi.web.rest.dto

import com.ritense.documentenapi.domain.ColumnDefaultSort
import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnId
import com.ritense.documentenapi.domain.DocumentenApiColumnKey
import com.ritense.documentenapi.domain.DocumentenApiVersion

data class ColumnResponse(
    val key: String,
    val sortable: Boolean,
    val filterable: Boolean,
    val defaultSort: String?,
) {
    fun toEntity(caseDefinitionName: String, order: Int = 0): DocumentenApiColumn {
        val keyEnum = DocumentenApiColumnKey.fromProperty(key)!!
        return DocumentenApiColumn(
            id = DocumentenApiColumnId(caseDefinitionName, keyEnum),
            order = order,
            defaultSort = defaultSort?.let { ColumnDefaultSort.valueOf(it.uppercase()) }
        )
    }

    companion object {
        fun of(column: DocumentenApiColumn, version: DocumentenApiVersion): ColumnResponse = ColumnResponse(
            key = column.id.key.property,
            sortable = version.isColumnSortable(column.id.key),
            filterable = version.isColumnFilterable(column.id.key),
            defaultSort = column.defaultSort?.name
        )
    }
}