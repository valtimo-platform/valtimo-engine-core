/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.search.security.config

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity

class SearchHttpSecurityConfigurer : HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/v1/search/list-column")
                .hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers(HttpMethod.GET, "/api/v1/search/list-column/{ownerId}")
                .hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers(HttpMethod.PUT, "/api/v1/search/list-column/{ownerId}/{key}")
                .hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers(HttpMethod.DELETE, "/api/v1/search/list-column/{ownerId}/{key}")
                .hasAuthority(AuthoritiesConstants.ADMIN)
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
