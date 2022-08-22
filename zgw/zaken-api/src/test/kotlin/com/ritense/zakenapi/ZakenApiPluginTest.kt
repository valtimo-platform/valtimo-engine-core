package com.ritense.zakenapi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.contract.resource.Resource
import com.ritense.zakenapi.client.LinkDocumentRequest
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zgw.Page
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.UUID
import kotlin.test.assertEquals

internal class ZakenApiPluginTest {

    @Test
    fun `should link document to zaak`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val resourceProvider: ResourceProvider = mock()
        val documentService: DocumentService = mock()
        val executionMock = mock<DelegateExecution>()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val documentId = UUID.randomUUID()
        whenever(executionMock.businessKey).thenReturn(documentId.toString())
        whenever(zaakUrlProvider.getZaak(any())).thenReturn("http://zaak.url")
        val mockResoure = mock<Resource>()
        whenever(resourceProvider.getResource(any())).thenReturn(mockResoure)
        val resourceId = UUID.randomUUID()
        whenever(mockResoure.id()).thenReturn(resourceId)

        val plugin = ZakenApiPlugin(zakenApiClient, zaakUrlProvider, resourceProvider, documentService)
        plugin.url = URI("http://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.linkDocumentToZaak(executionMock, "http://document.url", "titel", "beschrijving")

        val captor = argumentCaptor<LinkDocumentRequest>()
        verify(zakenApiClient).linkDocument(any(), any(), captor.capture())
        verify(documentService).assignResource(
            JsonSchemaDocumentId.existingId(documentId),
            resourceId,
            mapOf("createInformatieObject" to false)
        )

        val request = captor.firstValue
        assertEquals("http://document.url", request.informatieobject)
        assertEquals("http://zaak.url", request.zaak)
        assertEquals("titel", request.titel)
        assertEquals("beschrijving", request.beschrijving)
    }

    @Test
    fun `should return list of zaakobjecten`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val resourceProvider: ResourceProvider = mock()
        val documentService: DocumentService = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val resultPage = Page(
            2,
            null,
            null,
            listOf<ZaakObject>(
                mock(),
                mock()
            )
        )

        whenever(
            zakenApiClient.getZaakObjecten(
                authenticationMock,
                URI("http://zaken.plugin.url"),
                URI("http://example.org"),
                1
            )
        ).thenReturn(resultPage)

        val plugin = ZakenApiPlugin(zakenApiClient, zaakUrlProvider, resourceProvider, documentService)
        plugin.url = URI("http://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        val zaakUrl = URI("http://example.org")
        val zaakObjecten = plugin.getZaakObjecten(zaakUrl)

        assertEquals(2, zaakObjecten.size)
    }

    @Test
    fun `should return full list of zaakobjecten when multiple pages are found`() {
        val zakenApiClient: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val resourceProvider: ResourceProvider = mock()
        val documentService: DocumentService = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()

        val firstResultPage = Page(
            2,
            URI("http://zaken.plugin.url/zaken/api/v1/zaakobjecten?page=2"),
            null,
            listOf<ZaakObject>(
                mock(),
                mock()
            )
        )
        val secondResultPage = Page(
            1,
            null,
            URI("http://zaken.plugin.url/zaken/api/v1/zaakobjecten?page=1"),
            listOf<ZaakObject>(
                mock()
            )
        )

        whenever(
            zakenApiClient.getZaakObjecten(
                authenticationMock,
                URI("http://zaken.plugin.url"),
                URI("http://example.org"),
                1
            )
        ).thenReturn(firstResultPage)
        whenever(
            zakenApiClient.getZaakObjecten(
                authenticationMock,
                URI("http://zaken.plugin.url"),
                URI("http://example.org"),
                2
            )
        ).thenReturn(secondResultPage)

        val plugin = ZakenApiPlugin(zakenApiClient, zaakUrlProvider, resourceProvider, documentService)
        plugin.url = URI("http://zaken.plugin.url")
        plugin.authenticationPluginConfiguration = authenticationMock

        val zaakUrl = URI("http://example.org")
        val zaakObjecten = plugin.getZaakObjecten(zaakUrl)

        verify(zakenApiClient, times(2)).getZaakObjecten(any(), any(), any(), any())
        assertEquals(3, zaakObjecten.size)
    }
}