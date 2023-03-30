package com.ritense.authorization.permission

import com.ritense.authorization.Action
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class Permission(
    val resourceType: Class<*>,
    val action: Action,
    val filters: List<PermissionFilter>
) {
    fun appliesTo(resourceType: Class<*>, entity: Any?): Boolean {
        return if (this.resourceType.javaClass == resourceType.javaClass) {
            if (entity == null && filters.isNotEmpty()) {
                return false
            }
            filters
                .map { it.isValid(entity!!) }
                .all { it }
        } else {
            false
        }
    }

    fun <T> toPredicate(
        root: Root<T> ,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val predicates = filters.map {
            it.toPredicate(
                root,
                query,
                criteriaBuilder,
                resourceType,
                queryDialectHelper)
        }
        return criteriaBuilder
            .and(
                *filters.map {
                    it.toPredicate(
                        root,
                        query,
                        criteriaBuilder,
                        resourceType,
                        queryDialectHelper)
                }.toTypedArray()
            )
    }
}