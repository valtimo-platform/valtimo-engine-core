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

package com.ritense.processlink.security.config

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity

class ProcessLinkHttpSecurityConfigurer : HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeRequests()
                .requestMatchers(GET, "/api/v1/process-link").hasAuthority(ADMIN)
                .requestMatchers(GET, "/api/v1/process-link/types").hasAuthority(ADMIN)
                .requestMatchers(POST, "/api/v1/process-link").hasAuthority(ADMIN)
                .requestMatchers(PUT, "/api/v1/process-link").hasAuthority(ADMIN)
                .requestMatchers(GET, "/api/v1/process-link/export").hasAuthority(ADMIN)
                .requestMatchers(DELETE, "/api/v1/process-link/{processLinkId}").hasAuthority(ADMIN)
                .requestMatchers(GET, "/api/v2/process-link/task/{taskId}").authenticated()
                .requestMatchers(GET, "/api/v1/process-definition/{processDefinitionId}/start-form").authenticated()
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
