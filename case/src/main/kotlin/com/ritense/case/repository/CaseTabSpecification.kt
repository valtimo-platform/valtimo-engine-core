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

package com.ritense.case.repository

import com.ritense.authorization.AuthorizationContext
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.case.domain.CaseTab
import com.ritense.case.service.CaseTabService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import java.util.UUID
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

class CaseTabSpecification(
    authRequest: AuthorizationRequest<CaseTab>,
    permissions: List<Permission>,
    private val caseTabService: CaseTabService,
    private val queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecification<CaseTab>(authRequest, permissions) {
    override fun toPredicate(
        root: Root<CaseTab>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        // Filter the permissions for the relevant ones and use those to  find the filters that are required
        // Turn those filters into predicates
        val groupList = query.groupList.toMutableList()
        groupList.add(root.get<UUID>("id"))
        query.groupBy(groupList)

        val predicates = permissions.stream()
            .filter { permission: Permission ->
                CaseTab::class.java == permission.resourceType &&
                    authRequest.action == permission.action
            }
            .map { permission: Permission ->
                permission.toPredicate(
                    root,
                    query,
                    criteriaBuilder,
                    authRequest.resourceType,
                    queryDialectHelper
                )
            }.toList()
        return combinePredicates(criteriaBuilder, predicates)
    }

    override fun identifierToEntity(identifier: String): CaseTab {
        val caseDefinitionName = identifier.substringBefore(":")
        val key = identifier.substringAfter(":")
        return runWithoutAuthorization { caseTabService.getCaseTab(caseDefinitionName, key) }
    }
}

