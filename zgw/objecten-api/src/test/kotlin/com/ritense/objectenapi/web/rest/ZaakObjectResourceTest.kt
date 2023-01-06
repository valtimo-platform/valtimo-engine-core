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

package com.ritense.objectenapi.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.service.ZaakObjectService
import com.ritense.objectenapi.web.rest.result.ZaakInstanceLinkDTO
import com.ritense.objecttypenapi.client.Objecttype
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLinkId
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.valtimo.contract.json.Mapper
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.UUID

internal class ZaakObjectResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var zaakObjectService: ZaakObjectService
    lateinit var zaakObjectResource: ZaakObjectResource
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService

    @BeforeEach
    fun init() {
        zaakObjectService = mock()
        zaakInstanceLinkService = mock()
        zaakObjectResource = ZaakObjectResource(zaakObjectService, zaakInstanceLinkService)

        mockMvc = MockMvcBuilders
            .standaloneSetup(zaakObjectResource)
            .setMessageConverters(jacksonMessageConverter())
            .build()
    }

    @Test
    fun `should get objecttypes for documentId`() {
        val documentId = UUID.randomUUID()

        val type1 = mock<Objecttype>()
        whenever(type1.url).thenReturn(URI("http://example.com/1"))
        whenever(type1.name).thenReturn("name 1")

        val type2 = mock<Objecttype>()
        whenever(type2.url).thenReturn(URI("http://example.com/2"))
        whenever(type2.name).thenReturn("name 2")

        whenever(zaakObjectService.getZaakObjectTypes(documentId)).thenReturn(listOf(type1, type2))

        mockMvc
            .perform(
                get("/api/v1/document/$documentId/zaak/objecttype")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$.[0].url").value("http://example.com/1"))
            .andExpect(jsonPath("$.[1].url").value("http://example.com/2"))
            .andExpect(jsonPath("$.[0].name").value("name 1"))
            .andExpect(jsonPath("$.[1].name").value("name 2"))
    }

    @Test
    fun `should get objects for documentId and objecttype`() {
        val documentId = UUID.randomUUID()

        val object1 = mock<ObjectWrapper>()
        whenever(object1.url).thenReturn(URI("http://example.com/1"))
        val objectRecord1 = mock<ObjectRecord>()
        whenever(object1.record).thenReturn(objectRecord1)
        whenever(objectRecord1.index).thenReturn(1)
        whenever(objectRecord1.registrationAt).thenReturn(LocalDate.of(2020, 2, 3))
        whenever(objectRecord1.data).thenReturn(Mapper.INSTANCE.get().valueToTree(mapOf("title" to "some object")))

        val object2 = mock<ObjectWrapper>()
        whenever(object2.url).thenReturn(URI("http://example.com/2"))
        val objectRecord2 = mock<ObjectRecord>()
        whenever(object2.record).thenReturn(objectRecord2)
        whenever(objectRecord2.index).thenReturn(null)
        whenever(objectRecord2.registrationAt).thenReturn(null)
        whenever(objectRecord2.data).thenReturn(Mapper.INSTANCE.get().valueToTree(""))

        whenever(zaakObjectService.getZaakObjectenOfType(documentId, URI("http://example.com/objecttype")))
            .thenReturn(listOf(object1, object2))

        mockMvc
            .perform(
                get("/api/v1/document/$documentId/zaak/object?typeUrl=http://example.com/objecttype")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$.[0].url").value("http://example.com/1"))
            .andExpect(jsonPath("$.[1].url").value("http://example.com/2"))
            .andExpect(jsonPath("$.[0].index").value(1))
            .andExpect(jsonPath("$.[1].index").isEmpty)
            .andExpect(jsonPath("$.[0].registrationAt").value("2020-02-03"))
            .andExpect(jsonPath("$.[1].registrationAt").isEmpty)
            .andExpect(jsonPath("$.[0].title").value("some object"))
            .andExpect(jsonPath("$.[1].title").isEmpty)
    }

    @Test
    fun `should get form for object`() {
        val documentId = UUID.randomUUID()
        val formId = UUID.randomUUID()
        val objectUrl = URI("http://example.com/object")
        val formDefinition = FormIoFormDefinition(
            formId,
            "form-name",
            "{\"content\":\"test\"}",
            false
        )

        whenever(zaakObjectService.getZaakObjectForm(objectUrl)).thenReturn(formDefinition)

        mockMvc
            .perform(
                get("/api/v1/document/$documentId/zaak/object/form?objectUrl=$objectUrl")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.id").value(formId.toString()))
            .andExpect(jsonPath("$.name").value("form-name"))
            .andExpect(jsonPath("$.formDefinition.content").value("test"))
    }

    @Test
    fun `should return 404 when no form is found for object`() {
        val documentId = UUID.randomUUID()
        val objectUrl = URI("http://example.com/object")

        whenever(zaakObjectService.getZaakObjectForm(objectUrl)).thenReturn(null)

        mockMvc
            .perform(
                get("/api/v1/document/$documentId/zaak/object/form?objectUrl=$objectUrl")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound)
    }

    private fun jacksonMessageConverter(): MappingJackson2HttpMessageConverter {
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = Mapper.INSTANCE.get()
        return converter
    }

    @Test
    fun `should return ZaakInstaceLink for given zaakInstanceUrl`() {
        // given:
        val url = URI("http://localhost:8001/zaken/api/v1/zaken/df64a38e-566a-409d-8275-9207f70f79e7")
        val documentId = UUID.randomUUID()

        val entity = ZaakInstanceLink(
            zaakInstanceLinkId = ZaakInstanceLinkId(
                id = UUID.randomUUID()
            ),
            zaakInstanceUrl = url,
            zaakInstanceId = UUID.randomUUID(),
            documentId = documentId,
            zaakTypeUrl = URI("http://example.com"),
        )

        //when:
        whenever(zaakInstanceLinkService.getByZaakInstanceUrl(url)).thenReturn(
            entity
        )

        //then:
        val expectedDto = ZaakInstanceLinkDTO(
            zaakInstanceUrl = url,
            documentId = documentId
        )

        mockMvc.perform(
            get("/api/v1/zaakinstancelink/zaak?zaakInstanceUrl=$url")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.content().json(
                    jacksonObjectMapper().writeValueAsString(expectedDto)
                )
            )
    }

    @Test
    fun `should return ZaakInstaceLink for given documentId`() {
        // given:
        val url = URI("http://example1.com")
        val documentId = UUID.randomUUID()

        val result = ZaakInstanceLink(
            zaakInstanceLinkId = ZaakInstanceLinkId(
                id = UUID.randomUUID()
            ),
            zaakInstanceUrl = URI("http://example1.com"),
            zaakInstanceId = UUID.randomUUID(),
            documentId = documentId,
            zaakTypeUrl = URI("http://example2.com"),
        )

        //when:
        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(
            result
        )

        //then:
        val expectedDto = ZaakInstanceLinkDTO(
            zaakInstanceUrl = url,
            documentId = documentId
        )

        mockMvc.perform(
            get("/api/v1/zaakinstancelink/document?documentId=$documentId")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.content().json(
                    jacksonObjectMapper().writeValueAsString(expectedDto)
                )
            )

    }
}