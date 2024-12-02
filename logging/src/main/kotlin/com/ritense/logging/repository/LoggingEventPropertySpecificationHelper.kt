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

package com.ritense.logging.repository

import com.ritense.logging.domain.LoggingEventProperty
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.TIMESTAMP
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.time.ZoneId

class LoggingEventPropertySpecificationHelper {

    companion object {

        const val EVENT: String = "event"
        const val ID: String = "id"
        const val KEY: String = "key"
        const val VALUE: String = "value"

        @JvmStatic
        fun byOlderThan(localDateTime: LocalDateTime) = Specification<LoggingEventProperty> { root, _, cb ->
            val timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            cb.lessThan(root.join<Any, Any>(EVENT)[TIMESTAMP], timestamp)
        }

        @JvmStatic
        fun byKeyValue(key: String, value: String) = Specification<LoggingEventProperty> { root, _, cb ->
            cb.and(
                cb.equal(root.get<Any>(ID).get<Any>(KEY), key),
                cb.equal(root.get<Any>(VALUE), value),
            )
        }
    }
}