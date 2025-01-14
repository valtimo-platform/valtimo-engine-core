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

package com.ritense.notificatiesapi.domain

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Abonnement(
    val url: String? = null,
    val callbackUrl: String,
    val auth: String?,
    val kanalen: List<Kanaal> = listOf(),
) {
    data class Kanaal(
        val filters: Map<String, String> = mapOf(),
        val naam: String
    )

    fun equals(url: String, callbackUrl: String, kanalen: Set<String>): Boolean {
        return this.url == url
            && this.callbackUrl == callbackUrl
            && this.kanalen == kanalen.map { Kanaal(naam = it) }
    }
}