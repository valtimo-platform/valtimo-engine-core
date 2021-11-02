/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.service.impl.model.zaak

import com.fasterxml.jackson.annotation.JsonValue

enum class BetrokkeneType(@JsonValue val typeIdentifier: String) {
    NATUURLIJK_PERSOON("natuurlijk_persoon"),
    NIET_NATUURLIJK_PERSOON("niet_natuurlijk_persoon"),
    VESTIGING("vestiging"),
    ORGANISATORISCHE_EENHEID("organisatorische_eenheid"),
    MEDEWERKER("medewerker")
}