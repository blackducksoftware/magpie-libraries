/*
 * Copyright 2017 Black Duck Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blackducksoftware.common.security.auth;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.security.auth.callback.ChoiceCallback;

/**
 * A choice callback backed by an enumerated value.
 *
 * @author jgustie
 *
 * @param <E>
 *            the valid choices for this callback defined as an enum
 */
public class EnumChoiceCallback<E extends Enum<E>> extends ChoiceCallback {

    private static final long serialVersionUID = 5534324499615987270L;

    /**
     * The type of choices, needed to convert from selected indexes back to enum values.
     */
    private final Class<E> choices;

    public EnumChoiceCallback(String prompt, Class<E> choices, E defaultChoice,
            boolean multipleSelectionsAllowed, ResourceBundle bundle) {
        super(prompt, choices(choices, bundle), defaultChoice.ordinal(), multipleSelectionsAllowed);
        this.choices = Objects.requireNonNull(choices);
    }

    /**
     * Localizes the enumeration choices.
     */
    private static <E extends Enum<E>> String[] choices(Class<E> choices, ResourceBundle bundle) {
        return Stream.of(choices.getEnumConstants())
                .map(E::name)
                .map(bundle::getString)
                // TODO Use a prefix based on the class name?
                .toArray(String[]::new);
    }

    /**
     * Set the selected choice.
     */
    public void setSelectedChoice(E choice) {
        setSelectedIndex(choice.ordinal());
    }

    /**
     * Set the selected choices.
     */
    @SuppressWarnings("unchecked")
    public void setSelectedChoices(E... choices) {
        setSelectedIndexes(Stream.of(choices).mapToInt(E::ordinal).toArray());
    }

    /**
     * Set the selected choices.
     */
    public void setSelectedChoices(Iterable<E> choices) {
        setSelectedIndexes(StreamSupport.stream(choices.spliterator(), false).mapToInt(E::ordinal).toArray());
    }

    /**
     * Get the selected choice.
     */
    public Optional<E> getSelectedChoice() {
        int[] selectedIndexes = getSelectedIndexes();
        if (selectedIndexes != null && selectedIndexes.length > 0) {
            return IntStream.of(selectedIndexes)
                    .mapToObj(i -> choices.getEnumConstants()[i])
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get the selected choices.
     */
    public List<E> getSelectedChoices() {
        int[] selectedIndexes = getSelectedIndexes();
        if (selectedIndexes != null && selectedIndexes.length > 0) {
            return IntStream.of(selectedIndexes)
                    .mapToObj(i -> choices.getEnumConstants()[i])
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        } else {
            return Collections.emptyList();
        }
    }

}
