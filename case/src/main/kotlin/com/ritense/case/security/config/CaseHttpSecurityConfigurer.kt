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

package com.ritense.case.security.config

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity

class CaseHttpSecurityConfigurer : HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeHttpRequests { requests ->
                requests.requestMatchers(GET, "/api/v1/case/{caseDefinitionName}/list-column").authenticated()
                    .requestMatchers(POST, "/api/v1/case/{caseDefinitionName}/list-column")
                    .hasAuthority(ADMIN) // Deprecated
                    .requestMatchers(PUT, "/api/v1/case/{caseDefinitionName}/list-column")
                    .hasAuthority(ADMIN) // Deprecated
                    .requestMatchers(DELETE, "/api/v1/case/{caseDefinitionName}/list-column/{columnKey}")
                    .hasAuthority(ADMIN) // Deprecated
                    .requestMatchers(GET, "/api/v1/case/{caseDefinitionName}/settings").authenticated()
                    .requestMatchers(GET, "/api/v1/case-definition/{caseDefinitionName}/tab").authenticated()
                    .requestMatchers(PATCH, "/api/v1/case/{caseDefinitionName}/settings")
                    .hasAuthority(ADMIN) // Deprecated
                    .requestMatchers(POST, "/api/v1/case/{caseDefinitionName}/search").authenticated()
                    .requestMatchers(GET, "/api/management/v1/case/{caseDefinitionName}/settings").hasAuthority(ADMIN)
                    .requestMatchers(PATCH, "/api/management/v1/case/{caseDefinitionName}/settings").hasAuthority(ADMIN)
                    .requestMatchers(GET, "/api/management/v1/case/{caseDefinitionName}/list-column")
                    .hasAuthority(ADMIN)
                    .requestMatchers(POST, "/api/management/v1/case/{caseDefinitionName}/list-column")
                    .hasAuthority(ADMIN)
                    .requestMatchers(PUT, "/api/management/v1/case/{caseDefinitionName}/list-column")
                    .hasAuthority(ADMIN)
                    .requestMatchers(DELETE, "/api/management/v1/case/{caseDefinitionName}/list-column/{columnKey}")
                    .hasAuthority(ADMIN)
                    .requestMatchers(POST, "/api/management/v1/case-definition/{caseDefinitionName}/tab")
                    .hasAuthority(ADMIN)
                    .requestMatchers(PUT, "/api/management/v1/case-definition/{caseDefinitionName}/tab")
                    .hasAuthority(ADMIN)
                    .requestMatchers(PUT, "/api/management/v1/case-definition/{caseDefinitionName}/tab/{tabKey}")
                    .hasAuthority(ADMIN)
                    .requestMatchers(DELETE, "/api/management/v1/case-definition/{caseDefinitionName}/tab/{tabKey}")
                    .hasAuthority(ADMIN)
                    .requestMatchers(GET, "/api/management/v1/case-definition/{caseDefinitionName}/tab")
                    .hasAuthority(ADMIN)
                    .requestMatchers(GET, "/api/management/v1/case/{caseDefinitionName}/{caseDefinitionVersion}/export")
                    .hasAuthority(ADMIN)
            }
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
