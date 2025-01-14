/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.audit.service.impl;

import static java.time.LocalDateTime.now;

import com.ritense.audit.service.AuditRetentionService;
import com.ritense.audit.service.AuditService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

public class AuditRetentionServiceImpl implements AuditRetentionService {

    private final AuditService auditService;
    private final long retention;

    public AuditRetentionServiceImpl(AuditService auditService, long retention) {
        this.auditService = auditService;
        this.retention = retention;
    }

    @Override
    @Scheduled(cron = "${scheduling.job.cron.cleanupAuditEvents:-}")
    @SchedulerLock(
        name = "AuditRetentionService_cleanup", lockAtLeastFor = "PT4S", lockAtMostFor = "PT60M"
    )
    public void cleanup() {
        auditService.deleteAllBefore(now().minusDays(retention));
    }

}