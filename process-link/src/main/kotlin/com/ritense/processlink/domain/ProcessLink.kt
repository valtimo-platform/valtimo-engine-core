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

package com.ritense.processlink.domain

import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType.STRING
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType.SINGLE_TABLE
import jakarta.persistence.Table

@Entity
@Table(name = "process_link")
@Inheritance(strategy = SINGLE_TABLE)
@DiscriminatorColumn(name = "process_link_type", discriminatorType = STRING)
abstract class ProcessLink(

    @Id
    @Column(name = "id")
    open val id: UUID,

    @Column(name = "process_definition_id")
    open val processDefinitionId: String,

    @Column(name = "activity_id")
    open val activityId: String,

    @Column(name = "activity_type")
    @Enumerated(EnumType.STRING)
    open val activityType: ActivityTypeWithEventName,

    @Column(name = "process_link_type", insertable = false, updatable = false)
    open val processLinkType: String,
) {
    abstract fun copy(
        id: UUID = this.id,
        processDefinitionId: String = this.processDefinitionId,
    ) : ProcessLink
}
