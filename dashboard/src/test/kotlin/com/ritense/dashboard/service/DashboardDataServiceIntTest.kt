package com.ritense.dashboard.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.dashboard.BaseIntegrationTest
import com.ritense.dashboard.TestDataSource
import com.ritense.dashboard.TestDataSource.Companion.NUMBERS_DATA_KEY
import com.ritense.dashboard.TestDataSource.Companion.NUMBER_DATA_KEY
import com.ritense.dashboard.TestDataSourceProperties
import com.ritense.dashboard.TestWidgetNumberResult
import com.ritense.dashboard.TestWidgetNumbersResult
import com.ritense.dashboard.domain.WidgetConfiguration
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cache.CacheManager

class DashboardDataServiceIntTest @Autowired constructor(
    private val dashboardDataService: DashboardDataService,
    private val objectMapper: ObjectMapper,
    @SpyBean private val widgetConfigurationRepository: WidgetConfigurationRepository,
    @SpyBean private val testDataSource: TestDataSource,
    @SpyBean private val dashboardService: DashboardService,
    val cacheManager: CacheManager
): BaseIntegrationTest() {

    lateinit var numberConfiguration: WidgetConfiguration
    lateinit var numbersConfiguration: WidgetConfiguration

    @BeforeEach
    fun beforeEach() {
        clearCache()

        numberConfiguration = WidgetConfiguration(
            NUMBER_CONFIG_KEY, "", mock(), NUMBER_DATA_KEY,
            objectMapper.valueToTree(TestDataSourceProperties("xyz")),
            objectMapper.createObjectNode(),
            "", null, 0
        )
        numbersConfiguration = WidgetConfiguration(
            NUMBERS_CONFIG_KEY, "", mock(), NUMBERS_DATA_KEY,
            objectMapper.createObjectNode(), objectMapper.createObjectNode(),
            "", null,1
        )

        whenever(widgetConfigurationRepository.findAllByDashboardKey(DASHBOARD_KEY)).thenReturn(
            listOf(
                numberConfiguration,
                numbersConfiguration
            )
        )

        val testUser = ValtimoUser()
        testUser.firstName = "John"
        testUser.lastName = "Joe"
        whenever(userManagementService.currentUser).thenReturn(testUser)
    }

    @AfterEach
    fun afterEach() {
        clearCache()
    }

    private fun clearCache() {
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
    }
    @Test
    fun `should get data from test datasource`() {
        runWithoutAuthorization {
            dashboardService.createDashboard("Test", "Test description")
        }

        val dashboardData = dashboardDataService.getWidgetDataForDashboard(DASHBOARD_KEY)
        assertThat(dashboardData).hasSize(2)
        assertThat(dashboardData[0].data).isInstanceOf(TestWidgetNumberResult::class.java)
        assertThat(dashboardData[1].data).isInstanceOf(TestWidgetNumbersResult::class.java)

        verify(testDataSource, times(1)).numberData(any())
        verify(testDataSource, times(1)).numbersData()
    }

    @Test
    @Disabled
    fun `should cache widget data by dashboard key`() {
        val dashboardData = dashboardDataService.getWidgetDataForDashboard(DASHBOARD_KEY)

        val cachedDashboardData = dashboardDataService.getWidgetDataForDashboard(DASHBOARD_KEY)
        assertThat(cachedDashboardData[0]).isSameAs(dashboardData[0])
        assertThat(cachedDashboardData[1]).isSameAs(dashboardData[1])

        verify(testDataSource, times(1)).numberData(any())
        verify(testDataSource, times(1)).numbersData()
    }

    @Test
    @Disabled
    fun `should cache widget data by config key`() {
        val dashboardData = dashboardDataService.getWidgetDataForDashboard(DASHBOARD_KEY)

        val cachedNumber = dashboardDataService.getWidgetDataByConfigKey(NUMBER_CONFIG_KEY)
        assertThat(cachedNumber).isSameAs(dashboardData[0])
        val cachedNumbers = dashboardDataService.getWidgetDataByConfigKey(NUMBERS_CONFIG_KEY)
        assertThat(cachedNumbers).isSameAs(dashboardData[1])

        verify(testDataSource, times(1)).numberData(any())
        verify(testDataSource, times(1)).numbersData()
    }

    @Test
    @Disabled
    fun `should cache widget data by config instance`() {
        val dashboardData = dashboardDataService.getWidgetDataForDashboard(DASHBOARD_KEY)

        val cachedNumber = dashboardDataService.getWidgetDataByConfig(numberConfiguration)
        assertThat(cachedNumber).isSameAs(dashboardData[0])
        val cachedNumbers = dashboardDataService.getWidgetDataByConfig(numbersConfiguration)
        assertThat(cachedNumbers).isSameAs(dashboardData[1])

        verify(testDataSource, times(1)).numberData(any())
        verify(testDataSource, times(1)).numbersData()
    }

    companion object {
        private const val DASHBOARD_KEY = "test"
        private const val NUMBER_CONFIG_KEY = "number-config"
        private const val NUMBERS_CONFIG_KEY = "numbers-config"
    }
}