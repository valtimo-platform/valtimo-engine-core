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

package com.ritense.zakenapi.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.ritense.zgw.Rsin
import com.ritense.zgw.domain.Archiefnominatie
import com.ritense.zgw.domain.Vertrouwelijkheid
import java.net.URI
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PatchZaakRequest(
    val identificatie: String? = null,
    val bronorganisatie: Rsin? = null,
    val omschrijving: String? = null,
    val toelichting: String? = null,
    val zaaktype: URI? = null,
    val registratiedatum: LocalDate? = null,
    val verantwoordelijkeOrganisatie: Rsin? = null,
    val startdatum: LocalDate? = null,
    val einddatumGepland: LocalDate? = null,
    val uiterlijkeEinddatumAfdoening: LocalDate? = null,
    val publicatiedatum: LocalDate? = null,
    val communicatiekanaal: URI? = null,
    val productenOfDiensten: List<URI>? = null,
    val vertrouwelijkheidaanduiding: Vertrouwelijkheid? = null,
    val betalingsindicatie: Betalingsindicatie? = null,
    val laatsteBetaaldatum: LocalDate? = null,
    val zaakgeometrie: Geometry? = null,
    val verlenging: Verlenging? = null,
    val opschorting: Opschorting? = null,
    val selectielijstklasse: URI? = null,
    val hoofdzaak: URI? = null,
    val relevanteAndereZaken: List<RelevanteZaak>? = null,
    val kenmerken: List<Kenmerk>? = null,
    val archiefnominatie: Archiefnominatie? = null,
    val archiefstatus: Archiefstatus? = null,
    val archiefactiedatum: LocalDate? = null,
    val opdrachtgevendeOrganisatie: String? = null,
)