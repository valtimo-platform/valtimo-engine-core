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

package com.ritense.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.publisher.LoggingMessagePublisher
import com.ritense.outbox.publisher.MessagePublisher
import com.ritense.outbox.publisher.PollingPublisherJob
import com.ritense.outbox.publisher.PollingPublisherService
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@EnableJpaRepositories(
    basePackageClasses = [
        OutboxMessageRepository::class
    ]
)
@EntityScan(basePackages = ["com.ritense.outbox"])
@AutoConfigureAfter(DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class)
@EnableConfigurationProperties(LiquibaseProperties::class)
class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OutboxLiquibaseRunner::class)
    fun outboxLiquibaseRunner(
        liquibaseProperties: LiquibaseProperties,
        datasource: DataSource
    ): OutboxLiquibaseRunner {
        return OutboxLiquibaseRunner(liquibaseProperties, datasource)
    }

    @ConditionalOnMissingBean(UserProvider::class)
    @Bean
    fun userProvider(
    ): UserProvider {
        return UserProvider()
    }

    @Bean
    @ConditionalOnMissingBean(OutboxService::class)
    fun outboxService(
        outboxMessageRepository: OutboxMessageRepository,
        objectMapper: ObjectMapper,
        userProvider: UserProvider,
        @Value("\${valtimo.outbox.publisher.cloudevent-source:\${spring.application.name:application}}") cloudEventSource: String,
        ): OutboxService {
        return OutboxService(
            outboxMessageRepository,
            objectMapper,
            userProvider,
            cloudEventSource,
        )
    }

    @Bean
    @ConditionalOnMissingBean(PollingPublisherService::class)
    fun pollingPublisherService(
        outboxService: OutboxService,
        messagePublisher: MessagePublisher,
        platformTransactionManager: PlatformTransactionManager
    ): PollingPublisherService {
        return PollingPublisherService(
            outboxService,
            messagePublisher,
            platformTransactionManager
        )
    }

    @Bean
    @ConditionalOnMissingBean(PollingPublisherJob::class)
    fun pollingPublisherJob(
        pollingPublisherService: PollingPublisherService
    ): PollingPublisherJob {
        return PollingPublisherJob(pollingPublisherService)
    }

    @Bean
    @ConditionalOnMissingBean(MessagePublisher::class)
    fun loggingMessagePublisher(): MessagePublisher {
        return LoggingMessagePublisher()
    }

}
