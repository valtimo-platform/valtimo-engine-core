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

package com.ritense.processdocument.resolver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valueresolver.ValueResolverOptionType
import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.Collections
import java.util.Optional
import java.util.UUID

internal class DocumentJsonValueResolverTest {

    private lateinit var processDocumentService: ProcessDocumentService
    private lateinit var documentService: DocumentService
    private lateinit var documentDefinitionService: JsonSchemaDocumentDefinitionService

    private lateinit var documentValueResolver: DocumentJsonValueResolverFactory

    private lateinit var processInstanceId: String
    private lateinit var variableScope: DelegateTaskFake
    private lateinit var documentInstanceId: String
    private lateinit var document: Document

    @BeforeEach
    internal fun setUp() {
        processDocumentService = mock()
        documentService = mock()
        documentDefinitionService = mock()
        documentValueResolver = DocumentJsonValueResolverFactory(
            processDocumentService,
            documentService,
            documentDefinitionService,
            MapperSingleton.get()
        )

        processInstanceId = UUID.randomUUID().toString()
        variableScope = DelegateTaskFake()
        documentInstanceId = UUID.randomUUID().toString()
        document = mock()
        whenever(processDocumentService.getDocument(CamundaProcessInstanceId(processInstanceId), variableScope)).thenReturn(document)
        whenever(documentService.get(documentInstanceId)).thenReturn(document)
    }

    @Test
    fun `should resolve boolean value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "value": true, "lastName": "Doe"}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/value"
        )

        assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should resolve string value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/firstName"
        )

        assertThat(resolvedValue).isEqualTo("John")
    }

    @Test
    fun `should resolve int value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe", "age": 5}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/age"
        )

        assertThat(resolvedValue).isEqualTo(5)
    }

    @Test
    fun `should NOT resolve requestedValue from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/value"
        )

        assertThat(resolvedValue).isNull()
    }

    @Test
    fun `should resolve object-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"profile":{"firstName":"John"}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/profile"
        )

        assertThat(resolvedValue).isEqualTo(mapOf("firstName" to "John"))
    }

    @Test
    fun `should resolve array-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"cities":[{"name":"Amsterdam"},{"name":"Utrecht"}]}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/cities"
        )

        assertThat(resolvedValue).isEqualTo(listOf(mapOf("name" to "Amsterdam"), mapOf("name" to "Utrecht")))
    }

    @Test
    fun `should resolve empty-object-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root"
        )

        assertThat(resolvedValue).isEqualTo(emptyMap<String, Any>())
    }

    @Test
    fun `should resolve empty-array-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":[]}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root"
        )

        assertThat(resolvedValue).isEqualTo(emptyList<Any>())
    }

    @Test
    fun `should resolve null-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":null}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root"
        )

        assertThat(resolvedValue).isEqualTo(null)
    }

    @Test
    fun `should resolve missing-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root"
        )

        assertThat(resolvedValue).isEqualTo(null)
    }

    @Test
    fun `should resolve boolean value from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "value": true, "lastName": "Doe"}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "root.child.value"
        )

        assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should resolve string value from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "root.child.firstName"
        )

        assertThat(resolvedValue).isEqualTo("John")
    }

    @Test
    fun `should resolve int value from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe", "age": 5}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "root.child.age"
        )

        assertThat(resolvedValue).isEqualTo(5)
    }

    @Test
    fun `should NOT resolve requestedValue from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "root.child.value"
        )

        assertThat(resolvedValue).isNull()
    }

    @Test
    fun `should resolve object-value from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"profile":{"firstName":"John"}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "profile"
        )

        assertThat(resolvedValue).isEqualTo(mapOf("firstName" to "John"))
    }

    @Test
    fun `should resolve array-value from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"cities":[{"name":"Amsterdam"},{"name":"Utrecht"}]}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "cities"
        )

        assertThat(resolvedValue).isEqualTo(listOf(mapOf("name" to "Amsterdam"), mapOf("name" to "Utrecht")))
    }

    @Test
    fun `should resolve empty-object-value from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "root"
        )

        assertThat(resolvedValue).isEqualTo(emptyMap<String, Any>())
    }

    @Test
    fun `should resolve empty-array-value from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":[]}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "root"
        )

        assertThat(resolvedValue).isEqualTo(emptyList<Any>())
    }

    @Test
    fun `should resolve null-value from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":null}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "root"
        )

        assertThat(resolvedValue).isEqualTo(null)
    }

    @Test
    fun `should resolve missing-value from document properties for JsonPath`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{}"""))

        val resolvedValue = documentValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            "root"
        )

        assertThat(resolvedValue).isEqualTo(null)
    }


    @Test
    fun `should add text value`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/firstname" to "John")
        )

        val captor = argumentCaptor<JsonNode>()
        verify(documentService).modifyDocument(eq(document), captor.capture())
        assertThat(captor.firstValue).contains(TextNode.valueOf("John"))
    }

    @Test
    fun `should replace text value`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{\"firstname\":\"Peter\"}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/firstname" to "John")
        )

        val captor = argumentCaptor<JsonNode>()
        verify(documentService).modifyDocument(eq(document), captor.capture())
        assertThat(captor.firstValue).contains(TextNode.valueOf("John"))
    }

    @Test
    fun `should add boolean value`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/approved" to true)
        )

        val captor = argumentCaptor<JsonNode>()
        verify(documentService).modifyDocument(eq(document), captor.capture())
        assertThat(captor.firstValue).contains(BooleanNode.TRUE)
    }

    @Test
    fun `should add int value`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/age" to 18)
        )

        val captor = argumentCaptor<JsonNode>()
        verify(documentService).modifyDocument(eq(document), captor.capture())
        assertThat(captor.firstValue).contains(IntNode.valueOf(18))
    }

    @Test
    fun `should create JsonArray and JsonObject if not exist`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/a/-/b/-/c/-/firstname" to "John")
        )

        val captor = argumentCaptor<JsonNode>()
        verify(documentService).modifyDocument(eq(document), captor.capture())
        assertThat(captor.firstValue.at("/a/0/b/0/c/0/firstname")).isEqualTo(TextNode.valueOf("John"))
    }

    @Test
    fun `should replace value in list`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{\"myList\":[\"Peter\"]}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/myList/0" to "John")
        )

        val captor = argumentCaptor<JsonNode>()
        verify(documentService).modifyDocument(eq(document), captor.capture())
        assertThat(captor.firstValue.at("/myList/0")).isEqualTo(TextNode.valueOf("John"))
        assertThat(captor.firstValue.at("/myList/1")).isEqualTo(MissingNode.getInstance())
    }

    @Test
    fun `should add value to list`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{\"myList\":[\"Peter\"]}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/myList/-" to "John")
        )

        val captor = argumentCaptor<JsonNode>()
        verify(documentService).modifyDocument(eq(document), captor.capture())
        assertThat(captor.firstValue.at("/myList/0")).isEqualTo(TextNode.valueOf("Peter"))
        assertThat(captor.firstValue.at("/myList/1")).isEqualTo(TextNode.valueOf("John"))
    }

    @Test
    fun `should add object to list`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{\"myList\":[]}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/myList/-" to mapOf("field" to "My field", "list" to listOf("My item 1", "My item 2")))
        )

        val captor = argumentCaptor<JsonNode>()
        verify(documentService).modifyDocument(eq(document), captor.capture())
        val objectNode = MapperSingleton.get().readTree("{\"field\":\"My field\",\"list\":[\"My item 1\",\"My item 2\"]}")
        assertThat(captor.firstValue.at("/myList/0")).isEqualTo(objectNode)
    }

    @Test
    fun `should get property names from referenced nested object`() {
        val definitionName = "combined-schema-additional-property-example"
        mockDefinition(definitionName)

        val paths = documentValueResolver.getResolvableKeyOptions(definitionName)
            .map { it.path }

        Collections.sort(paths)
        Assertions.assertArrayEquals(
            paths.toTypedArray(), arrayOf(
                "doc:/address/city",
                "doc:/address/country",
                "doc:/address/number",
                "doc:/address/province",
                "doc:/address/streetName"
            )
        )
    }

    @Test
    fun `should get array property with nested properties`() {
        val definitionName = "nested-array-example"
        mockDefinition(definitionName)

        val options = documentValueResolver.getResolvableKeyOptions(definitionName)

        assertEquals(1, options.size)
        assertEquals(ValueResolverOptionType.COLLECTION, options[0].type)
        assertEquals("doc:/object1/object2/array1", options[0].path)
        assertNotNull(options[0].children)
        assertEquals(1, options[0].children?.size)
        assertEquals(ValueResolverOptionType.FIELD, options[0].children?.get(0)?.type)
        assertEquals("/object3/object4/text1", options[0].children?.get(0)?.path)
    }

    @Test
    fun `should get array property with nested properties when using reference`() {
        val definitionName = "nested-array-reference-example"
        mockDefinition(definitionName)

        val options = documentValueResolver.getResolvableKeyOptions(definitionName)

        assertEquals(1, options.size)
        assertEquals(ValueResolverOptionType.COLLECTION, options[0].type)
        assertEquals("doc:/object1/object2/array1", options[0].path)
        assertNotNull(options[0].children)
        assertEquals(1, options[0].children?.size)
        assertEquals(ValueResolverOptionType.FIELD, options[0].children?.get(0)?.type)
        assertEquals("/object3/object4/text1", options[0].children?.get(0)?.path)
    }

    private fun mockDefinition(definitionName: String?): JsonSchemaDocumentDefinition {
        val definition: JsonSchemaDocumentDefinition = definitionOf(definitionName)
        whenever(documentDefinitionService.findLatestByName(definitionName)).thenReturn(Optional.of(definition))
        return definition
    }

    private fun definitionOf(name: String?): JsonSchemaDocumentDefinition {
        val documentDefinitionName = JsonSchemaDocumentDefinitionId.newId(name)
        val schema = JsonSchema.fromResourceUri(path(documentDefinitionName.name()))
        return JsonSchemaDocumentDefinition(documentDefinitionName, schema)
    }

    private fun path(name: String): URI {
        return URI.create(String.format("config/document/definition/%s.json", "$name.schema"))
    }
}
