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

package com.ritense.document.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RelatedFile {

    @JsonProperty
    UUID getFileId();

    @JsonProperty
    String getFileName();

    @JsonProperty
    Long getSizeInBytes();

    @JsonProperty
    LocalDateTime getCreatedOn();

    @JsonProperty
    String getCreatedBy();

    @JsonProperty
    String getAuthor();

    @JsonProperty
    String getTitle();

    @JsonProperty
    String getStatus();

    @JsonProperty
    String getLanguage();

    @JsonProperty
    String getIdentification();

    @JsonProperty
    String getDescription();
    @JsonProperty
    String getInformatieobjecttype();

    List<String> getKeywords();

    String getFormat();

    LocalDate getSendDate();

    LocalDate getReceiptDate();

    String getConfidentialityLevel();

}
