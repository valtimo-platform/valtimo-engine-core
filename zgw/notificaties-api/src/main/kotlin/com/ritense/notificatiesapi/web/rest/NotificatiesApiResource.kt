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

package com.ritense.notificatiesapi.web.rest

import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.notificatiesapi.exception.AuthorizationException
import com.ritense.notificatiesapi.service.NotificatiesApiService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class NotificatiesApiResource(
    private val notificatiesApiService: NotificatiesApiService
) {

    @PostMapping("/v1/notificatiesapi/callback")
    fun handleNotification(
        @RequestBody notification: NotificatiesApiNotificationReceivedEvent,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Void> {
        return if (authHeader != null) {
            if (notification.kanaal == "test") {
                return ResponseEntity.noContent().build()
            }
            try {
                notificatiesApiService.findAbonnementSubscription(authHeader)
                notificatiesApiService.handle(notification)
                ResponseEntity.noContent().build()
            } catch (ex: AuthorizationException) {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }

        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }
}
