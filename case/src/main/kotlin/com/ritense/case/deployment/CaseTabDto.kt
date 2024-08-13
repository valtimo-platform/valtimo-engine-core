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

package com.ritense.case.deployment

import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabType

data class CaseTabDto(
    val key: String,
    val name: String? = null,
    val type: CaseTabType,
    val contentKey: String
) {
    companion object {
        fun of(caseTab: CaseTab) = CaseTabDto(
            caseTab.id.key,
            caseTab.name,
            caseTab.type,
            caseTab.contentKey
        )
    }
}