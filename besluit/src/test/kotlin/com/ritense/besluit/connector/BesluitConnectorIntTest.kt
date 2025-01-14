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

package com.ritense.besluit.connector

import com.ritense.besluit.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.UUID

class BesluitConnectorIntTest : BaseIntegrationTest() {

    @Test
    fun `should create besluit and publish event`() {
        val besluit = besluitConnector.createBesluit(
            URI("http://zaakInstanceUri.com"),
            URI("http://besluittypeUri.com"),
            UUID.randomUUID().toString()
        )

        assertNotNull(besluit)
        assertEquals(
            URI("http://example/api/v1/besluiten/16d33b53-e283-40ef-8d86-6914282aea25"),
            besluit.url)
        assertNotNull(besluit.identificatie)
    }
}