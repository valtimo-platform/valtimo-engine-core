package com.ritense.case.web.dto

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.case_.domain.definition.CaseDefinitionId
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CaseSettingsDtoTest {
    @Test
    fun `should update case settings when value is not null`() {
        val currentCaseWithSettings = CaseDefinition(CaseDefinitionId("key", "1.0.0"), "name")
        val caseSettingsDto = CaseSettingsDto(canHaveAssignee = true)
        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)

        assertFalse(currentCaseWithSettings.canHaveAssignee)
        assertTrue(caseSettingsDto.canHaveAssignee!!)
        assertTrue(updatedCaseSettings.canHaveAssignee)
    }

    @Test
    fun `should not update case settings when value is null`() {
        val currentCaseWithSettings = CaseDefinition(CaseDefinitionId("key", "1.0.0"), "name")
        val caseSettingsDto = CaseSettingsDto()
        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)


        assertFalse(currentCaseWithSettings.canHaveAssignee)
        assertNull(caseSettingsDto.canHaveAssignee)
        assertFalse(updatedCaseSettings.canHaveAssignee)
    }

    @Test
    fun `should set autoAssignTasks to false when canHaveAssignee is set to false`() {
        val currentCaseWithSettings = CaseDefinition(
            CaseDefinitionId("key", "1.0.0"),
            "name",
            canHaveAssignee = true,
            autoAssignTasks = true
        )
        val caseSettingsDto = CaseSettingsDto(
            canHaveAssignee = false
        )
        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)

        assertTrue(currentCaseWithSettings.canHaveAssignee)
        assertTrue(currentCaseWithSettings.autoAssignTasks)
        assertNull(caseSettingsDto.autoAssignTasks)
        assertFalse(updatedCaseSettings.canHaveAssignee)
        assertFalse(updatedCaseSettings.autoAssignTasks)
    }
}