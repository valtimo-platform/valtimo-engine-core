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

package com.ritense.processlink.domain

import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.processlink.domain.TestProcessLink.Companion.PROCESS_LINK_TYPE_TEST
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import com.ritense.valtimo.contract.json.MapperSingleton
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.util.UUID

class TestProcessLinkUpdateRequestDtoTest {

    private val mapper = MapperSingleton.get().copy().apply {
        this.registerSubtypes(TestProcessLinkCreateRequestDto::class.java, TestProcessLinkUpdateRequestDto::class.java)
    }

    @Test
    fun `should deserialise correctly with processLinkType`() {
        val value: ProcessLinkUpdateRequestDto = mapper.readValue("""
            {
                "id": "${UUID.randomUUID()}",
                "processLinkType": "$PROCESS_LINK_TYPE_TEST",
                "someValue": "test"
            }
        """.trimIndent())

        assertThat(value, instanceOf(TestProcessLinkUpdateRequestDto::class.java))
        value as TestProcessLinkUpdateRequestDto
        assertThat(value.processLinkType, equalTo(PROCESS_LINK_TYPE_TEST))
        assertThat(value.someValue, equalTo("test"))
    }

    @Test
    fun `should deserialise correctly without processLinkType`() {
        val value: ProcessLinkUpdateRequestDto = mapper.readValue("""
            {
                "id": "${UUID.randomUUID()}",
                "someValue": "test"
            }
        """.trimIndent())

        assertThat(value, instanceOf(TestProcessLinkUpdateRequestDto::class.java))
        value as TestProcessLinkUpdateRequestDto
        assertThat(value.processLinkType, equalTo(PROCESS_LINK_TYPE_TEST))
        assertThat(value.someValue, equalTo("test"))
    }

    @Test
    fun `should serialize correctly`() {
        val value = TestProcessLinkUpdateRequestDto(UUID.randomUUID(), "test")

        val json = mapper.writeValueAsString(value)

        JSONAssert.assertEquals("""
            {
              "id":"${value.id}",
              "someValue": "test",
              "processLinkType": "$PROCESS_LINK_TYPE_TEST",
            }
        """.trimIndent(), json, JSONCompareMode.NON_EXTENSIBLE)
    }
}
