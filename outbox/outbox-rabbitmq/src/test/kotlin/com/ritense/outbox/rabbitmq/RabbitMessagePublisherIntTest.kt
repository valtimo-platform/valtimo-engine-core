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

package com.ritense.outbox.rabbitmq

import com.ritense.outbox.OutboxMessage
import com.ritense.outbox.rabbitmq.config.RabbitOutboxConfigurationProperties
import java.util.UUID
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

class RabbitMessagePublisherIntTest {
    @Nested
    inner class Default @Autowired constructor(
        val springCloudMessagePublisher: RabbitMessagePublisher,
        val rabbitTemplate: RabbitTemplate,
        val configurationProperties: RabbitOutboxConfigurationProperties,
        val rabbitAdmin: RabbitAdmin
    ) : BaseIntegrationTest() {
        @Test
        fun `should send message to rabbitmq`() {
            rabbitAdmin.purgeQueue(configurationProperties.queueName)

            val uuid = UUID.randomUUID().toString()
            springCloudMessagePublisher.publish(
                OutboxMessage(message = uuid)
            )

            val msg = rabbitTemplate.receive(configurationProperties.queueName)
            Assertions.assertThat(msg.body.toString(Charsets.UTF_8)).isEqualTo(uuid)
        }
    }

    @Nested
    @ActiveProfiles("invalidqueue")
    inner class InvalidQueue @Autowired constructor(
        val springCloudMessagePublisher: RabbitMessagePublisher
    ) : BaseIntegrationTest() {
        @Test
        fun `should not send message to rabbitmq`() {
            val uuid = UUID.randomUUID().toString()
            val ex = assertThrows<RuntimeException> {
                springCloudMessagePublisher.publish(
                    OutboxMessage(message = uuid)
                )
            }

            Assertions.assertThat(ex.message).contains("NO_ROUTE")
        }
    }
}