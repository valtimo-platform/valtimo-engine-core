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

package com.ritense.formviewmodel.web.rest.error

import com.ritense.formviewmodel.error.BusinessException
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.error.MultipleFormException
import com.ritense.formviewmodel.web.rest.dto.MultipleFormError
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest

@SkipComponentScan
@ControllerAdvice
class FormViewModelModuleExceptionTranslator {

    @ExceptionHandler(FormException::class)
    fun handleFormException(
        ex: FormException,
        request: NativeWebRequest
    ): ResponseEntity<MultipleFormError> {
        return ResponseEntity
            .badRequest()
            .body(
                MultipleFormError(
                    listOf(
                        MultipleFormException.ComponentError(
                            component = ex.component,
                            message = ex.message ?: UNKNOWN_FORM_ERROR
                        )
                    )
                )
            )
    }

    @ExceptionHandler(MultipleFormException::class)
    fun handleFormException(
        ex: MultipleFormException,
        request: NativeWebRequest
    ): ResponseEntity<MultipleFormError> {
        return ResponseEntity
            .badRequest()
            .body(
                MultipleFormError(ex.componentErrors)
            )
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: NativeWebRequest
    ): ResponseEntity<MultipleFormError> {
        return ResponseEntity
            .badRequest()
            .body(
                MultipleFormError(
                    listOf(
                        MultipleFormException.ComponentError(
                            component = null,
                            message = ex.message ?: UNKNOWN_BUSINESS_RULE_ERROR
                        )
                    )
                )
            )
    }

    companion object {
        private const val UNKNOWN_FORM_ERROR = "Unknown Form Error"
        private const val UNKNOWN_BUSINESS_RULE_ERROR = "Unknown Business Rule Error"
    }
}