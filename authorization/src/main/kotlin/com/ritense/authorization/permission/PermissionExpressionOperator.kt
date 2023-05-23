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

package com.ritense.authorization.permission

import com.fasterxml.jackson.annotation.JsonValue
import kotlin.reflect.full.isSubclassOf

enum class PermissionExpressionOperator(
    @JsonValue val asText: String,
    private val notEqualCompareResult:Int = 1
) {
    NOT_EQUAL_TO("!="),
    EQUAL_TO("=="),
    GREATER_THAN(">", -1),
    GREATER_THAN_OR_EQUAL_TO(">=", -1),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL_TO("<=");

    private class NullableComparator<T: Comparable<T>>(private val notEqualResult: Int): Comparator<T?> {
        override fun compare(left: T?, right: T?): Int {
            return if (left == right) {
                0
            } else if (
                left == null ||
                right == null ||
                (!left::class.isSubclassOf(right::class))
            ) {
                notEqualResult
            } else {
                left.compareTo(right)
            }
        }
    }
}