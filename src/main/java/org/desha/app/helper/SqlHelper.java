package org.desha.app.helper;

import io.quarkus.panache.common.Parameters;
import org.desha.app.domain.dto.CriteriasDTO;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class SqlHelper {

    SqlHelper() {
    }

    public static Parameters addParameters(Parameters params, CriteriasDTO criteriasDTO) {
        /*Map<String, Supplier<Object>> criteriaMap = Map.ofEntries(
                Map.entry("fromReleaseDate", criteriasDTO::getFromReleaseDate),
                Map.entry("toReleaseDate", criteriasDTO::getToReleaseDate),
                Map.entry("fromCreationDate", criteriasDTO::getFromCreationDate),
                Map.entry("toCreationDate", criteriasDTO::getToCreationDate),
                Map.entry("fromLastUpdate", criteriasDTO::getFromLastUpdate),
                Map.entry("toLastUpdate", criteriasDTO::getToLastUpdate),
                Map.entry("fromBirthDate", criteriasDTO::getFromBirthDate),
                Map.entry("toBirthDate", criteriasDTO::getToBirthDate),
                Map.entry("fromDeathDate", criteriasDTO::getFromDeathDate),
                Map.entry("toDeathDate", criteriasDTO::getToDeathDate),
                Map.entry("genreIds", criteriasDTO::getGenreIds),
                Map.entry("countryIds", criteriasDTO::getCountryIds),
                Map.entry("userIds", criteriasDTO::getUserIds),
                Map.entry("personTypes", criteriasDTO::getPersonTypes)
        );

        criteriaMap.forEach((key, supplier) -> {
            Object value = supplier.get();
            if (value instanceof Collection<?> collection) {
                if (!collection.isEmpty()) {
                    params.and(key, collection);
                }
            } else if (Objects.nonNull(value)) {
                params.and(key, value);
            }
        });*/

        if (Objects.nonNull(criteriasDTO.getFromReleaseDate())) {
            params.and("fromReleaseDate", criteriasDTO.getFromReleaseDate());
        }
        if (Objects.nonNull(criteriasDTO.getToReleaseDate())) {
            params.and("toReleaseDate", criteriasDTO.getToReleaseDate());
        }
        if (Objects.nonNull(criteriasDTO.getFromBirthDate())) {
            params.and("fromBirthDate", criteriasDTO.getFromBirthDate());
        }
        if (Objects.nonNull(criteriasDTO.getToBirthDate())) {
            params.and("toBirthDate", criteriasDTO.getToBirthDate());
        }
        if (Objects.nonNull(criteriasDTO.getFromDeathDate())) {
            params.and("fromDeathDate", criteriasDTO.getFromDeathDate());
        }
        if (Objects.nonNull(criteriasDTO.getToDeathDate())) {
            params.and("toDeathDate", criteriasDTO.getToDeathDate());
        }
        if (Objects.nonNull(criteriasDTO.getFromCreationDate())) {
            params.and("fromCreationDate", criteriasDTO.getFromCreationDate());
        }
        if (Objects.nonNull(criteriasDTO.getToCreationDate())) {
            params.and("toCreationDate", criteriasDTO.getToCreationDate());
        }
        if (Objects.nonNull(criteriasDTO.getFromLastUpdate())) {
            params.and("fromLastUpdate", criteriasDTO.getFromLastUpdate());
        }
        if (Objects.nonNull(criteriasDTO.getToLastUpdate())) {
            params.and("toLastUpdate", criteriasDTO.getToLastUpdate());
        }
        if (Objects.nonNull(criteriasDTO.getGenreIds()) && !criteriasDTO.getGenreIds().isEmpty()) {
            params.and("genreIds", criteriasDTO.getGenreIds());
        }
        if (Objects.nonNull(criteriasDTO.getCountryIds()) && !criteriasDTO.getCountryIds().isEmpty()) {
            params.and("countryIds", criteriasDTO.getCountryIds());
        }
        if (Objects.nonNull(criteriasDTO.getUserIds()) && !criteriasDTO.getUserIds().isEmpty()) {
            params.and("userIds", criteriasDTO.getUserIds());
        }
        if (Objects.nonNull(criteriasDTO.getPersonTypes()) && !criteriasDTO.getPersonTypes().isEmpty()) {
            params.and("personTypes", criteriasDTO.getPersonTypes());
        }

        return params;
    }
}
