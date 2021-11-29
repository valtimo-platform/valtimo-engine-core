/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.mail.domain.filters

import com.ritense.mail.config.MailingProperties
import com.ritense.mail.service.BlacklistService
import com.ritense.valtimo.contract.mail.MailFilter
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage

class BlacklistFilter(
    private val mailingProperties: MailingProperties,
    private val blacklistService: BlacklistService
) : MailFilter {

    override fun apply(rawMailMessage: RawMailMessage): RawMailMessage {
        rawMailMessage
            .recipients
            .filterBy { !blacklistService.isBlacklisted(it.email.get()) }
        return rawMailMessage
    }

    override fun apply(templatedMailMessage: TemplatedMailMessage): TemplatedMailMessage {
        templatedMailMessage
            .recipients
            .filterBy { !blacklistService.isBlacklisted(it.email.get()) }
        return templatedMailMessage
    }

    override fun isEnabled(): Boolean {
        return mailingProperties.isBlacklistFilter
    }

    override fun getPriority(): Int {
        return mailingProperties.blacklistFilterPriority
    }

}