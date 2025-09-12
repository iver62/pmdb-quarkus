package org.desha.app.helper;

import io.quarkus.panache.common.Parameters;
import org.desha.app.domain.dto.CriteriaDTO;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class SqlHelper {

    public Parameters addParameters(Parameters params, CriteriaDTO criteriaDTO) {
        Map<String, Supplier<Object>> criteriaMap = Map.ofEntries(
                Map.entry("fromReleaseDate", criteriaDTO::getFromReleaseDate),
                Map.entry("toReleaseDate", criteriaDTO::getToReleaseDate),
                Map.entry("fromCreationDate", criteriaDTO::getFromCreationDate),
                Map.entry("toCreationDate", criteriaDTO::getToCreationDate),
                Map.entry("fromLastUpdate", criteriaDTO::getFromLastUpdate),
                Map.entry("toLastUpdate", criteriaDTO::getToLastUpdate),
                Map.entry("fromBirthDate", criteriaDTO::getFromBirthDate),
                Map.entry("toBirthDate", criteriaDTO::getToBirthDate),
                Map.entry("fromDeathDate", criteriaDTO::getFromDeathDate),
                Map.entry("toDeathDate", criteriaDTO::getToDeathDate),
                Map.entry("categoryIds", criteriaDTO::getCategoryIds),
                Map.entry("countryIds", criteriaDTO::getCountryIds),
                Map.entry("userIds", criteriaDTO::getUserIds),
                Map.entry("personTypes", criteriaDTO::getPersonTypes)
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
        });

//        if (Objects.nonNull(criteriaDTO.getFromReleaseDate())) {
//            params.and("fromReleaseDate", criteriaDTO.getFromReleaseDate());
//        }
//        if (Objects.nonNull(criteriaDTO.getToReleaseDate())) {
//            params.and("toReleaseDate", criteriaDTO.getToReleaseDate());
//        }
//        if (Objects.nonNull(criteriaDTO.getFromBirthDate())) {
//            params.and("fromBirthDate", criteriaDTO.getFromBirthDate());
//        }
//        if (Objects.nonNull(criteriaDTO.getToBirthDate())) {
//            params.and("toBirthDate", criteriaDTO.getToBirthDate());
//        }
//        if (Objects.nonNull(criteriaDTO.getFromDeathDate())) {
//            params.and("fromDeathDate", criteriaDTO.getFromDeathDate());
//        }
//        if (Objects.nonNull(criteriaDTO.getToDeathDate())) {
//            params.and("toDeathDate", criteriaDTO.getToDeathDate());
//        }
//        if (Objects.nonNull(criteriaDTO.getFromCreationDate())) {
//            params.and("fromCreationDate", criteriaDTO.getFromCreationDate());
//        }
//        if (Objects.nonNull(criteriaDTO.getToCreationDate())) {
//            params.and("toCreationDate", criteriaDTO.getToCreationDate());
//        }
//        if (Objects.nonNull(criteriaDTO.getFromLastUpdate())) {
//            params.and("fromLastUpdate", criteriaDTO.getFromLastUpdate());
//        }
//        if (Objects.nonNull(criteriaDTO.getToLastUpdate())) {
//            params.and("toLastUpdate", criteriaDTO.getToLastUpdate());
//        }
//        if (Objects.nonNull(criteriaDTO.getCategoryIds()) && !criteriaDTO.getCategoryIds().isEmpty()) {
//            params.and("categoryIds", criteriaDTO.getCategoryIds());
//        }
//        if (Objects.nonNull(criteriaDTO.getCountryIds()) && !criteriaDTO.getCountryIds().isEmpty()) {
//            params.and("countryIds", criteriaDTO.getCountryIds());
//        }
//        if (Objects.nonNull(criteriaDTO.getUserIds()) && !criteriaDTO.getUserIds().isEmpty()) {
//            params.and("userIds", criteriaDTO.getUserIds());
//        }
//        if (Objects.nonNull(criteriaDTO.getPersonTypes()) && !criteriaDTO.getPersonTypes().isEmpty()) {
//            params.and("personTypes", criteriaDTO.getPersonTypes());
//        }

        return params;
    }
}
