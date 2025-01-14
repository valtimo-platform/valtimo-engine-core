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

package com.ritense.document.service;

import com.ritense.authorization.Action;
import com.ritense.authorization.ResourceActionProvider;
import com.ritense.document.domain.impl.searchfield.SearchField;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SearchFieldActionProvider implements ResourceActionProvider<SearchField> {
    public static final Action<SearchField> VIEW_LIST = new Action<>(Action.VIEW_LIST);

    @NotNull
    @Override
    public List<Action<SearchField>> getAvailableActions() {
        return List.of(VIEW_LIST);
    }
}
