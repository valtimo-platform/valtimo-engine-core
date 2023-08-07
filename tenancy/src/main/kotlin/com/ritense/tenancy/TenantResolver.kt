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

package com.ritense.tenancy;

import com.ritense.tenancy.authentication.TenantAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class TenantResolver {
    /**
     * Resolve tenant-id from auth
     * @return Tenant id as string not empty
     */
    fun getTenantId(): String {
        val tenantId = (SecurityContextHolder.getContext().authentication as TenantAuthenticationToken).tenantId
        require(tenantId.isNotEmpty()) { "'tenantId' can not be empty" }
        return tenantId
    }

/*    companion object {
        @JvmField
        val INSTANCE = TenantResolver()
    }*/

}