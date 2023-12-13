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

package com.ritense.case.service

import com.ritense.importer.ImportRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class CaseDefinitionSettingsImporterTest(
    @Mock private val deploymentService: CaseDefinitionDeploymentService
) {
    private lateinit var importer: CaseDefinitionSettingsImporter

    @BeforeEach
    fun before() {
        importer = CaseDefinitionSettingsImporter(deploymentService)
    }

    @Test
    fun `should be of type 'casesettings'`() {
        assertThat(importer.type()).isEqualTo("casesettings")
    }

    @Test
    fun `should depend on 'documentdefinition' type`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf("documentdefinition"))
    }

    @Test
    fun `should support casesettings fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-caselist fileName`() {
        assertThat(importer.supports("config/case/definition/x/test.json")).isFalse()
        assertThat(importer.supports("config/case/definition/test-json")).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = "{}"
        importer.import(ImportRequest(FILENAME, jsonContent.toByteArray()))

        val nameCaptor = argumentCaptor<String>()
        val jsonCaptor = argumentCaptor<String>()

        verify(deploymentService).deploy(nameCaptor.capture(), jsonCaptor.capture())

        assertThat(nameCaptor.firstValue).isEqualTo("my-case-list")
        assertThat(jsonCaptor.firstValue).isEqualTo(jsonContent)
    }

    private companion object {
        const val FILENAME = "config/case/definition/my-case-list.json"
    }
}