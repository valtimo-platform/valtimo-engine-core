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

package com.ritense.notificatiesapi

import com.ritense.notificatiesapi.client.NotificatiesApiClient
import com.ritense.notificatiesapi.domain.Abonnement
import com.ritense.notificatiesapi.domain.Kanaal
import com.ritense.notificatiesapi.domain.NotificatiesApiAbonnementLink
import com.ritense.notificatiesapi.domain.NotificatiesApiConfigurationId
import com.ritense.notificatiesapi.repository.AbonnementLinkRepository
import com.ritense.plugin.domain.PluginConfigurationId
import java.net.URI
import java.util.Optional
import java.util.UUID
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

internal class NotificatiesApiPluginTest {
    lateinit var notificatiesApiClient: NotificatiesApiClient
    lateinit var abonnementLinkRepository: AbonnementLinkRepository
    lateinit var plugin: NotificatiesApiPlugin

    @BeforeEach
    fun setup() {
        notificatiesApiClient = mock()
        abonnementLinkRepository = mock()

        plugin = NotificatiesApiPlugin(notificatiesApiClient, abonnementLinkRepository)
            .apply {
                url = URI("http://example.com")
                authenticationPluginConfiguration = mock()
            }
    }


    @Test
    fun `ensure kanaal exists creates kanaal when kanaal doesnt exist`(): Unit = runBlocking {
        whenever(notificatiesApiClient.getKanalen(any(), any()))
            .thenReturn(
                listOf(
                    Kanaal(naam = "objecten")
                )
            )

        plugin.ensureKanalenExist(setOf("objecten", "taken", "test-kanaal"))

        verify(notificatiesApiClient, times(1)).getKanalen(any(), any())
        verify(notificatiesApiClient, times(2)).createKanaal(any(), any(), any())
    }

    @Test
    fun `ensure kanaal exists doesnt create kanaal when kanaal exists`(): Unit = runBlocking {

        whenever(notificatiesApiClient.getKanalen(any(), any()))
            .thenReturn(
                listOf(
                    Kanaal(naam = "objecten"),
                    Kanaal(naam = "test-kanaal"),
                    Kanaal(naam = "taken")
                )
            )

        plugin.ensureKanalenExist(setOf("objecten", "taken", "test-kanaal"))

        verify(notificatiesApiClient, times(1)).getKanalen(any(), any())
        verify(notificatiesApiClient, never()).createKanaal(any(), any(), any())
    }

    @Test
    fun `createAbonnement should create abonnement and save entity`(): Unit = runBlocking {
        val pluginId = PluginConfigurationId(UUID.randomUUID())
        val abonnementId = UUID.randomUUID()
        val abonnement = Abonnement(
            url = "http://example.com/abonnement/$abonnementId",
            callbackUrl = "http://example.com/callback",
            auth = "some-key",
            kanalen = listOf(
                Abonnement.Kanaal(
                    naam = "test-kanaal"
                )
            )
        )
        val linkCaptor = ArgumentCaptor.forClass(NotificatiesApiAbonnementLink::class.java)

        whenever(notificatiesApiClient.createAbonnement(any(), any(), any()))
            .thenReturn(abonnement)
        whenever(notificatiesApiClient.getKanalen(any(), any()))
            .thenReturn(
                listOf(
                    Kanaal(naam = "test-kanaal")
                )
            )

        plugin.createAbonnement(
            callbackUrl = "http://example.com/callback",
            pluginConfigurationId = pluginId,
            kanaalNames = setOf("test-kanaal")
        )

        verify(abonnementLinkRepository, times(1)).save(linkCaptor.capture())
        assertContains(linkCaptor.value.url, abonnementId.toString())
        assertEquals("some-key", linkCaptor.value.auth)
    }

    @Test
    fun `deleteAbonnement should delete abonnement in Notificaties API and repository`() {
        val pluginId = PluginConfigurationId(UUID.randomUUID())
        val abonnementId = UUID.randomUUID()
        val notificatiesApiConfigurationIdCaptor = ArgumentCaptor.forClass(NotificatiesApiConfigurationId::class.java)
        val abonnementLink = NotificatiesApiAbonnementLink(
            pluginConfigurationId = pluginId,
            url = "http://example.com/abonnement/$abonnementId",
            auth = "some-key"
        )

        whenever(abonnementLinkRepository.findById(notificatiesApiConfigurationIdCaptor.capture()))
            .thenReturn(
                Optional.of(abonnementLink)
            )

        plugin.deleteAbonnement(pluginConfigurationId = pluginId)

        verifyBlocking(notificatiesApiClient, times(1)) {
            deleteAbonnement(
                plugin.authenticationPluginConfiguration, plugin.url, abonnementId.toString()
            )
        }
        verify(abonnementLinkRepository, times(1)).deleteById(notificatiesApiConfigurationIdCaptor.value)
    }

    @Test
    fun `deleteAbonnement should do nothing if abonnement link does not exist`() {
        val pluginId = PluginConfigurationId(UUID.randomUUID())

        whenever(abonnementLinkRepository.findById(any()))
            .thenReturn(
                Optional.empty()
            )

        plugin.deleteAbonnement(pluginConfigurationId = pluginId)

        verifyBlocking(notificatiesApiClient, never()) {
            deleteAbonnement(any(), any(), any())
        }
        verify(abonnementLinkRepository, never()).deleteById(any())
    }
}