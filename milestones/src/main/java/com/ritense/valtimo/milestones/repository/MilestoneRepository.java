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

package com.ritense.valtimo.milestones.repository;

import com.ritense.valtimo.milestones.domain.Milestone;
import com.ritense.valtimo.milestones.domain.MilestoneSet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    Milestone getMilestoneByTitle(String title);

    List<Milestone> findMilestonesByMilestoneSet(MilestoneSet milestoneSet);

    Milestone findMilestoneByTaskDefinitionKeyAndProcessDefinitionKey(String taskDefinitionKey, String processDefinitionKey);

    Optional<Milestone> findFirstByMilestoneSetId(Long milestoneSetId);
}
