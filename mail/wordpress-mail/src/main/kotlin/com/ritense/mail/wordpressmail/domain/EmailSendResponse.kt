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

package com.ritense.mail.wordpressmail.domain

import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.mail.model.MailMessageStatus

data class EmailSendResponse(
    val success: Boolean? = false,
    val message: String?,
    val data: Data,
) {
    data class Data(
        val recipient: String,
        val subject: String,
        val content: String,
    )

    fun toMailMessageStatus(): MailMessageStatus {
        return MailMessageStatus
            .with(getRecipientEmailAddress(), "SENT", hashCode().toString())
            .build()
    }

    private fun getRecipientEmailAddress(): EmailAddress {
        return EmailAddress.from(Regex("<(.+?)>").find(data.recipient)!!.groupValues[1])
    }
}
