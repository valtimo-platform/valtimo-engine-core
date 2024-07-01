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

package com.ritense.processdocument.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.document.service.result.CreateDocumentResult
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.domain.CaseTask
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.tasksearch.SearchWithConfigRequest
import com.ritense.processdocument.tasksearch.TaskListSearchFieldV2Dto
import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.service.SearchFieldV2Service
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseTaskListSearchServiceIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var caseTaskListSearchService: CaseTaskListSearchService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var searchFieldV2Service: SearchFieldV2Service

    private var definition: JsonSchemaDocumentDefinition? = null

    private var originalDocument: CreateDocumentResult? = null

    @BeforeEach
    fun init() {
        definition = definition()
        val content = JsonDocumentContent("{\"street\": \"Funenpark\", \"houseNumber\": 1, \"isEnrolled\": \"yes\"}")

        originalDocument = runWithoutAuthorization<CreateDocumentResult> {
            val result: CreateDocumentResult = documentService.createDocument(
                NewDocumentRequest(
                    definition!!.id().name(),
                    content.asJson()
                )
            )
            result
        }

        val content2 = JsonDocumentContent("{\"street\": \"Kalverstraat\"}")

        runWithoutAuthorization<CreateDocumentResult> {
            val result: CreateDocumentResult = documentService.createDocument(
                NewDocumentRequest(
                    definition!!.id().name(),
                    content2.asJson()
                )
            )
            result
        }

        searchFieldV2Service.create(TaskListSearchFieldV2Dto(
            id = UUID.randomUUID(),
            ownerId = definition!!.id!!.name(),
            key = "street",
            title = "Street",
            path = "doc:street",
            order = 1,
            dataType = DataType.TEXT,
            fieldType = FieldType.TEXT_CONTAINS,
            matchType = SearchFieldMatchType.LIKE,
            dropdownDataProvider = null)
        )

        searchFieldV2Service.create(TaskListSearchFieldV2Dto(
            id = UUID.randomUUID(),
            ownerId = definition!!.id!!.name(),
            key = "number",
            title = "House number",
            path = "doc:houseNumber",
            order = 1,
            dataType = DataType.NUMBER,
            fieldType = FieldType.RANGE,
            matchType = null,
            dropdownDataProvider = null)
        )

        runWithoutAuthorization {
            camundaProcessJsonSchemaDocumentService.startProcessForDocument(
                StartProcessForDocumentRequest(
                    originalDocument!!.resultingDocument().orElseThrow().id(),
                    "loan-process-demo",
                    mapOf()
                )
            )
        }
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun shouldFindTaskByStreet() {
        val filter = SearchWithConfigRequest.SearchWithConfigFilter()
        filter.key = "street"
        filter.setValues(listOf("Funenpark"))

        val searchResult = searchTasks(filter)
        assertThat(searchResult).hasSize(1)

        val matchedResult = searchResult!!.content[0]
        assertThat(matchedResult.name).isEqualTo("Akkoord op lening?")
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun shouldNotFindTaskByStreet() {
        val filter = SearchWithConfigRequest.SearchWithConfigFilter()
        filter.key = "street"
        filter.setValues(listOf("Herengracht"))

        val searchResult = searchTasks(filter)
        assertThat(searchResult).isEmpty()
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun shouldFindTaskByHouseNumberRange() {
        val filter = SearchWithConfigRequest.SearchWithConfigFilter()
        filter.key = "number"
        filter.setRangeFrom(0)
        filter.setRangeTo(10)

        val searchResult = searchTasks(filter)
        assertThat(searchResult).hasSize(1)

        val matchedResult = searchResult!!.content[0]
        assertThat(matchedResult.name).isEqualTo("Akkoord op lening?")
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun shouldNotFindTaskByHouseNumberRange() {
        val filter = SearchWithConfigRequest.SearchWithConfigFilter()
        filter.key = "number"
        filter.setRangeFrom(10)
        filter.setRangeTo(20)

        val searchResult = searchTasks(filter)
        assertThat(searchResult).hasSize(0)
    }

    private fun searchTasks(
        filter: SearchWithConfigRequest.SearchWithConfigFilter
    ): Page<CaseTask>? {
        val searchWithConfigRequest = SearchWithConfigRequest()

        searchWithConfigRequest.otherFilters = listOf(filter)

        val searchResult = runWithoutAuthorization {
            caseTaskListSearchService.search("house", searchWithConfigRequest, PageRequest.of(0, 50))
        }
        return searchResult
    }
}