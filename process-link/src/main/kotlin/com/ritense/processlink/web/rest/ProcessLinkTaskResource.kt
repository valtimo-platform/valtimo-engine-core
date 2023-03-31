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

package com.ritense.processlink.web.rest

import com.ritense.processlink.service.ProcessLinkTaskService
import com.ritense.processlink.web.rest.dto.OpenTaskResult
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api"])
class ProcessLinkTaskResource(
    private var processLinkTaskService: ProcessLinkTaskService
) {
    @GetMapping(value = ["/v2/process-link/task/{taskId}"])
    fun getTask(@PathVariable taskId: UUID): ResponseEntity<OpenTaskResult<Any>> {
        return ResponseEntity.ok(processLinkTaskService.openTask(taskId))
    }
}
