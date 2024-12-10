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

package com.ritense.case_.domain.definition

import com.ritense.valtimo.contract.case_.CaseDefinitionId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "case_definition")
data class CaseDefinition(
    @EmbeddedId
    val id: CaseDefinitionId, // TODO: Determine if we want to enforce semver for the version tag https://github.com/semver4j/semver4j
    @Column(name = "case_definition_name")
    val name: String,
    @Column(name = "can_have_assignee")
    val canHaveAssignee: Boolean = false,
    @Column(name = "auto_assign_tasks")
    val autoAssignTasks: Boolean = false,
) {
    init {
        require(
            when (autoAssignTasks) {
                true -> canHaveAssignee
                else -> true
            }
        ) { "Case property [autoAssignTasks] can only be true when [canHaveAssignee] is true." }
    }
}