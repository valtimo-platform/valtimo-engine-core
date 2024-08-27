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

package com.ritense.logging

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import java.lang.reflect.Method


@Aspect
class LoggableResourceAspect {

    @Around("execution(* *(.., @com.ritense.logging.LoggableResource (*), ..))")
    fun handleAnnotation(joinPoint: ProceedingJoinPoint): Any {
        val args = joinPoint.args

        val method: Method = MethodSignature::class.java.cast(joinPoint.signature).method
        val parameterAnnotations = method.parameterAnnotations

        for (i in args.indices) {
            for (parameterAnnotation in parameterAnnotations[i]) {
                if (parameterAnnotation !is LoggableResource) {
                    continue
                }

                return ResourceLogger.withResource(parameterAnnotation.resourceType.java, args[i] as String) {
                    joinPoint.proceed()
                }

                // how to handle multiple arguments with this annotation?
            }
        }

        return ResourceLogger.withResource(String::class.java, "12331231") {
            joinPoint.proceed()
        }
    }
}
