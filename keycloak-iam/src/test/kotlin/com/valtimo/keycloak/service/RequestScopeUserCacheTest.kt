package com.valtimo.keycloak.service

import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class RequestScopeUserCacheTest {
     @Test
     fun `should return user information from cache`() {
         val cacheType = RequestScopeUserCache.CacheType.EMAIL
         val key = "key"
         val expectedResult = mock<ManageableUser>()
         val requestFunction: (String) -> ManageableUser? = { expectedResult }
         val cache = RequestScopeUserCache()

         val result = cache.get(cacheType, key, requestFunction)

         assertEquals(expectedResult, result)
     }

    @Test
    fun `should only call retrieval function once`() {
        // Arrange
        val cacheType = RequestScopeUserCache.CacheType.EMAIL
        val key = "key"
        val expectedResult = mock<ManageableUser>()
        val mockRequestFunction = mock<UserRetrievalFunction>()
        val requestFunction: (String) -> ManageableUser? = {
            mockRequestFunction.retrieveUser(key)
            expectedResult
        }

        val cache = RequestScopeUserCache()

        val result = cache.get(cacheType, key, requestFunction)
        val result2 = cache.get(cacheType, key, requestFunction)
        assertEquals(expectedResult, result)
        assertEquals(expectedResult, result2)
        verify(mockRequestFunction, times(1)).retrieveUser(key)
    }

    @Test
    fun `should use separate cache for different types`() {
        val cacheType1 = RequestScopeUserCache.CacheType.EMAIL
        val cacheType2 = RequestScopeUserCache.CacheType.USER_IDENTIFIER
        val key = "key"
        val mockRequestFunction = mock<UserRetrievalFunction>()
        // need 2 functions because of different return types, but we want to use the same mock to verify it gets called twice
        val expectedResult1 = mock<ManageableUser>()
        val requestFunction1: (String) -> ManageableUser? = {
            mockRequestFunction.retrieveUser(key)
            expectedResult1
        }
        val expectedResult2 = mock<ValtimoUser>()
        val requestFunction2: (String) -> ValtimoUser? = {
            mockRequestFunction.retrieveUser(key)
            expectedResult2
        }

        val cache = RequestScopeUserCache()

        val result1 = cache.get(cacheType1, key, requestFunction1)
        val result2 = cache.get(cacheType2, key, requestFunction2)
        assertEquals(expectedResult1, result1)
        assertEquals(expectedResult2, result2)
        verify(mockRequestFunction, times(2)).retrieveUser(key)
    }

    @Test
    fun `should only cache expected types`() {
        val cacheType = RequestScopeUserCache.CacheType.EMAIL
        val key = "key"
        val requestFunction: (String) -> String? = { key }
        val cache = RequestScopeUserCache()

        val ex = assertThrows<IllegalArgumentException> {
            cache.get(cacheType, key, requestFunction)
        }

        assertEquals(
            "The type of the value returned by the request function (java.lang.String) does not match the cache type (com.ritense.valtimo.contract.authentication.ManageableUser)",
            ex.message
        )
    }

    interface UserRetrievalFunction {
        fun retrieveUser(key: String): Void
    }
 }