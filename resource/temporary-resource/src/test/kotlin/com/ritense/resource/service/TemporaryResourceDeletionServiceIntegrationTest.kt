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

package com.ritense.resource.service

import com.ritense.resource.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.time.Instant
import kotlin.io.path.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TemporaryResourceDeletionServiceIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var temporaryResourceService: TemporaryResourceService

    @Autowired
    lateinit var temporaryResourceDeletionService: TemporaryResourceDeletionService

    @Test
    fun `should delete files older that 5 minutes`() {
        val resourceId = temporaryResourceService.store("My file data".byteInputStream())
        val attributes = Files.getFileAttributeView(Path(resourceId), BasicFileAttributeView::class.java)
        val time = FileTime.from(Instant.now().minus(Duration.ofMinutes(5)))
        attributes.setTimes(time, time, time)

        temporaryResourceDeletionService.deleteOldTemporaryResources()

        val exception = assertThrows<IllegalArgumentException> {
            temporaryResourceService.getResourceContentAsInputStream(resourceId)
        }
        assertThat(exception.message).isEqualTo("No resource found with id '$resourceId'")
    }

}
