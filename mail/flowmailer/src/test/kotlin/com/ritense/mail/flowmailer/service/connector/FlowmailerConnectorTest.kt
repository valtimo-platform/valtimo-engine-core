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

package com.ritense.mail.flowmailer.service.connector

import com.ritense.mail.flowmailer.BaseTest
import com.ritense.mail.flowmailer.config.FlowmailerProperties
import com.ritense.mail.flowmailer.service.FlowmailerMailDispatcher
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class FlowmailerConnectorTest: BaseTest() {
    lateinit var flowmailerConnectorProperties: FlowmailerConnectorProperties
    lateinit var flowmailerMailDispatcher: FlowmailerMailDispatcher
    lateinit var flowmailerConnector: FlowmailerConnector

    @BeforeEach
    fun setup() {
        val flowmailerProperties = FlowmailerProperties(
            clientId = "clientId",
            clientSecret = "clientSecret",
            accountId = "accountId"
        )
        flowmailerConnectorProperties = FlowmailerConnectorProperties(flowmailerProperties)
        flowmailerMailDispatcher = mock(FlowmailerMailDispatcher::class.java)
        flowmailerConnector = FlowmailerConnector(
            flowmailerConnectorProperties = flowmailerConnectorProperties,
            mailDispatcher = flowmailerMailDispatcher
        )
    }

    @Test
    fun `should get properties`() {
        //when
        val properties = flowmailerConnector.getProperties()
        //then
        assertThat(properties).isNotNull
    }

    @Test
    fun `should set properties`() {
        //Given
        val flowmailerProperties = FlowmailerProperties(
            clientId = "newClientId",
            clientSecret = "clientSecret",
            accountId = "accountId"
        )
        //when
        flowmailerConnector.setProperties(flowmailerConnectorProperties)
        val properties = flowmailerConnector.getProperties()
        //then
        assertThat(properties).isNotNull  //TODO: how to check the clientId from a ConnectorProperties?
    }

    @Test
    fun `should call the send method from the FlowmailerDispatcher`() {
        //Given
        val templatedMailMessage = templatedMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            )
        )
        //When
        flowmailerConnector.sendEmail(templatedMailMessage)
        //Then
        verify(flowmailerMailDispatcher, times(1)).send(templatedMailMessage)
    }

}